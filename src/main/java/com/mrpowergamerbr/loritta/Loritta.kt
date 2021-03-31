package com.mrpowergamerbr.loritta

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.salomonbrys.kotson.*
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.CommandManager
import com.mrpowergamerbr.loritta.frontend.views.GlobalHandler
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.threads.*
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.StoredMessage
import com.mrpowergamerbr.loritta.utils.gabriela.GabrielaMessage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper
import com.mrpowergamerbr.loritta.utils.music.GuildMusicManager
import com.mrpowergamerbr.loritta.utils.temmieyoutube.TemmieYouTube
import com.mrpowergamerbr.temmiemercadopago.TemmieMercadoPago
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.managers.AudioManager
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.*
import kotlin.concurrent.thread

/**
 * Classe principal da Loritta
 */
class Loritta {
	// ===[ STATIC ]===
	companion object {
		// ===[ LORITTA ]===
		@JvmStatic
		lateinit var config: LorittaConfig // Configuração da Loritta
		@JvmField
		var FOLDER = "/home/servers/loritta/" // Pasta usada na Loritta
		@JvmField
		var ASSETS = "/home/servers/loritta/assets/" // Pasta de assets da Loritta
		@JvmField
		var TEMP = "/home/servers/loritta/temp/" // Pasta usada para coisas temporarias
		@JvmField
		var LOCALES = "/home/servers/loritta/locales/" // Pasta usada para as locales
		@JvmField
		var FRONTEND = "/home/servers/loritta/frontend/" // Pasta usada para as locales
		@JvmStatic
		var temmieMercadoPago: TemmieMercadoPago? = null // Usado na página de "doar"

		// ===[ UTILS ]===
		@JvmStatic
		val RANDOM = SplittableRandom() // Um splittable RANDOM global, para não precisar ficar criando vários (menos GC)
		@JvmStatic
		var GSON = Gson() // Gson
		@JvmStatic
		val JSON_PARSER = JsonParser() // Json Parser
		@JvmStatic
		lateinit var youtube: TemmieYouTube // API key do YouTube, usado em alguns comandos

		val logger = LoggerFactory.getLogger(Loritta::class.java)
	}

	// ===[ LORITTA ]===
	var lorittaShards = LorittaShards() // Shards da Loritta
	val eventLogExecutors = createThreadPool("Event Log Thread %d") // Threads
	val messageExecutors = createThreadPool("Message Thread %d") // Threads
	val executor = createThreadPool("Executor Thread %d") // Threads
	val socket = SocketServer(10699)

	lateinit var commandManager: CommandManager // Nosso command manager
	lateinit var dummyServerConfig: ServerConfig // Config utilizada em comandos no privado
	var messageContextCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterAccess(5L, TimeUnit.MINUTES).build<String, CommandContext>().asMap()
	var messageInteractionCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterAccess(5L, TimeUnit.MINUTES).build<String, MessageInteractionFunctions>().asMap()

	fun createThreadPool(name: String): ExecutorService {
		return Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())
	}

	var locales = mutableMapOf<String, BaseLocale>()
	var ignoreIds = mutableListOf<String>() // IDs para serem ignorados nesta sessão
	val userCooldown = CacheBuilder.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<String, Long>().asMap()
	val apiCooldown = CacheBuilder.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<String, Long>().asMap()

	var southAmericaMemesPageCache = mutableListOf<FacebookPostWrapper>()
	var southAmericaMemesGroupCache = mutableListOf<FacebookPostWrapper>()

	// ===[ MONGODB ]===
	lateinit var mongo: MongoClient // MongoDB
	lateinit var serversColl: MongoCollection<ServerConfig>
	lateinit var usersColl: MongoCollection<LorittaProfile>
	lateinit var storedMessagesColl: MongoCollection<StoredMessage>
	lateinit var gabrielaMessagesColl: MongoCollection<GabrielaMessage>

	// ===[ MÚSICA ]===
	lateinit var playerManager: AudioPlayerManager
	lateinit var musicManagers: MutableMap<Long, GuildMusicManager>
	var songThrottle = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterAccess(5L, TimeUnit.MINUTES).build<String, Long>().asMap()

	var youtubeKeys: MutableList<String> = mutableListOf<String>()
	var lastKeyReset = 0

	var fanArts = mutableListOf<LorittaFanArt>()
	var discordListener = DiscordListener(this) // Vamos usar a mesma instância para todas as shards
	var eventLogListener = EventLogListener(this) // Vamos usar a mesma instância para todas as shards
	var builder: JDABuilder

	val log = File(FOLDER, "log-${System.currentTimeMillis()}.log")

	var userCount = 0
	var guildCount = 0

	lateinit var loteriaThread: LoteriaThread

	// Constructor da Loritta
	constructor(config: LorittaConfig) {
		FOLDER = config.lorittaFolder
		ASSETS = config.assetsFolder
		TEMP = config.tempFolder
		LOCALES = config.localesFolder
		FRONTEND = config.frontendFolder

		Loritta.config = config // Salvar a nossa configuração na variável Loritta#config
		loadLocales()
		Loritta.temmieMercadoPago = TemmieMercadoPago(config.mercadoPagoClientId, config.mercadoPagoClientToken) // Iniciar o client do MercadoPago
		Loritta.youtube = TemmieYouTube()
		resetYouTubeKeys()
		loadFanArts()
		GlobalHandler.generateViews()

		builder = JDABuilder(AccountType.BOT)
				.setToken(Loritta.config.clientToken)
				.setCorePoolSize(64)
				.setBulkDeleteSplittingEnabled(false)

		builder.addEventListener(discordListener)
		builder.addEventListener(eventLogListener)
	}

	// Gera uma configuração "dummy" para comandos enviados no privado
	fun generateDummyServerConfig() {
		val dummy = ServerConfig("-1").apply { // É usado -1 porque -1 é um número de guild inexistente
			commandPrefix = ""
			mentionOnCommandOutput = false
		}

		dummyServerConfig = dummy
	}

	fun resetYouTubeKeys() {
		youtubeKeys.clear()
		youtubeKeys.addAll(config.youtubeKeys)
		lastKeyReset = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
	}

	val youtubeKey: String
		get() {
			if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] != lastKeyReset) {
				if (Calendar.getInstance()[Calendar.HOUR_OF_DAY] == 4) {
					resetYouTubeKeys()
				}
			}
			return youtubeKeys[RANDOM.nextInt(youtubeKeys.size)]
		}

	// Inicia a Loritta
	fun start() {
		// Mandar o MongoDB calar a boca
		val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
		val rootLogger = loggerContext.getLogger("org.mongodb.driver")
		rootLogger.level = Level.OFF

		initMongo()

		generateDummyServerConfig()

		println("Sucesso! Iniciando Loritta (Discord Bot)...") // Agora iremos iniciar o bot

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		val generateShards = Loritta.config.shards - 1

		loadCommandManager() // Inicie todos os comandos da Loritta

		for (idx in 0..generateShards) {
			println("Iniciando Shard $idx...")
			val shard = builder
					.useSharding(idx, Loritta.config.shards)
					.buildAsync()

			lorittaShards.shards.add(shard)
			println("Shard $idx iniciada com sucesso!")
		}

		println("Sucesso! Iniciando Loritta (Website)...") // E agora iremos iniciar o website da Loritta

		val website = thread(true, name = "Website Thread") {
			org.jooby.run({ com.mrpowergamerbr.loritta.frontend.LorittaWebsite(config.websiteUrl, config.frontendFolder) })
		}

		println("Sucesso! Iniciando threads da Loritta...")

		NewLivestreamThread.isLivestreaming = GSON.fromJson(File(Loritta.FOLDER, "livestreaming.json").readText())

		thread {
			socket.start()
		}

		// AminoRepostThread().start() // Iniciar Amino Repost Thread

		// NewYouTubeVideosThread().start() // Iniciar New YouTube Videos Thread

		// NewLivestreamThread().start() // Iniciar New Livestream Thread

		// NewRssFeedThread().start() // Iniciar Feed Rss

		UpdateStatusThread().start() // Iniciar thread para atualizar o status da Loritta

		// DiscordBotsInfoThread().start() // Iniciar thread para atualizar os servidores no Discord Bots

		// FetchFacebookPostsThread().start() // Iniciar thread para pegar posts do Facebook

		RemindersThread().start()

		MutedUsersThread().start() // Iniciar thread para desmutar usuários e desbanir usuários temporariamente banidos

		val loteriaFile = File(FOLDER, "loteria.json")

		if (loteriaFile.exists()) {
			val json = JSON_PARSER.parse(loteriaFile.readText())

			LoteriaThread.started = json["started"].long
			LoteriaThread.lastWinnerId = json["lastWinnerId"].string
			LoteriaThread.lastWinnerPrize = json["lastWinnerPrize"].int
			LoteriaThread.userIds = GSON.fromJson(json["userIds"])
		}

		loteriaThread = LoteriaThread()
		loteriaThread.start()

		DebugLog.startCommandListenerThread()

		// MutedUsersThread().start() // Iniciar thread para desmutar usuários e desbanir usuários temporariamente banidos

		// GiveawayThread().start() // Iniciar thread para processar giveaways

		thread(name = "Small Update Stuff") {
			while (true) {
				try {
					userCount = lorittaShards.getUserCount()
					guildCount = lorittaShards.getGuildCount()
				} catch (e: Exception) {
					e.printStackTrace()
				}

				Thread.sleep(15000)
			}
		}

		musicManagers = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterAccess(5L, TimeUnit.MINUTES).build<Long, GuildMusicManager>().asMap()
		playerManager = DefaultAudioPlayerManager()

		val trackInfoExecutorServiceField = playerManager::class.java.getDeclaredField("trackInfoExecutorService")

		trackInfoExecutorServiceField.isAccessible = true
		val trackInfoExecutorService = trackInfoExecutorServiceField.get(playerManager) as ThreadPoolExecutor
		trackInfoExecutorService.maximumPoolSize = 100

		AudioSourceManagers.registerRemoteSources(playerManager)
		AudioSourceManagers.registerLocalSource(playerManager)

		LorittaUtilsKotlin.startAutoPlaylist()
		// Ou seja, agora a Loritta está funcionando, Yay!
	}

	fun initMongo() {
		println("Iniciando MongoDB...")

		val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

		val mongoBuilder = MongoClientOptions.Builder().apply {
			codecRegistry(pojoCodecRegistry)
		}

		val options = mongoBuilder
				.maxConnectionIdleTime(10000)
				.maxConnectionLifeTime(10000)
				.connectionsPerHost(750)
				.build()

		mongo = MongoClient("127.0.0.1:27017", options) // Hora de iniciar o MongoClient

		val db = mongo.getDatabase("loritta")

		val dbCodec = db.withCodecRegistry(pojoCodecRegistry)

		serversColl = dbCodec.getCollection("servers", ServerConfig::class.java)
		usersColl = dbCodec.getCollection("users", LorittaProfile::class.java)
		storedMessagesColl = dbCodec.getCollection("storedmessages", StoredMessage::class.java)
		gabrielaMessagesColl = dbCodec.getCollection("gabriela", GabrielaMessage::class.java)
	}

	/**
	 * Carrega um ServerConfig de uma guild
	 *
	 * @param guildId
	 * @return ServerConfig
	 */
	fun getServerConfigForGuild(guildId: String): ServerConfig {
		val serverConfig = serversColl.find(Filters.eq("_id", guildId)).first()
		return serverConfig ?: ServerConfig(guildId)
	}

	/**
	 * Carrega um LorittaProfile de um usuário
	 *
	 * @param userId
	 * @return LorittaProfile
	 */
	fun getLorittaProfileForUser(userId: String): LorittaProfile {
		val userProfile = usersColl.find(Filters.eq("_id", userId)).first()
		return userProfile ?: LorittaProfile(userId)
	}

	/**
	 * Cria o CommandManager
	 */
	fun loadCommandManager() {
		// Isto parece não ter nenhuma utilidade, mas, caso estejamos usando o JRebel, é usado para recarregar o command manager
		// Ou seja, é possível adicionar comandos sem ter que reiniciar tudo!
		commandManager = CommandManager()
	}

	fun loadFanArts() {
		fanArts = GSON.fromJson(File("./fanarts.json").readText())
	}

	/**
	 * Inicia os locales da Loritta
	 */
	fun loadLocales() {
		val locales = mutableMapOf<String, BaseLocale>()

		// Carregar primeiro o locale padrão
		val defaultLocaleFile = File(Loritta.LOCALES, "default.json")
		val localeAsText = defaultLocaleFile.readText(Charsets.UTF_8)
		val defaultLocale = GSON.fromJson(localeAsText, BaseLocale::class.java) // Carregar locale do jeito velho
		val defaultJsonLocale = JSON_PARSER.parse(localeAsText).obj // Mas também parsear como JSON

		defaultJsonLocale.entrySet().forEach { (key, value) ->
			if (!value.isJsonArray) { // TODO: Listas!
				defaultLocale.strings.put(key, value.string)
			}
		}

		// E depois guardar o nosso default locale
		locales.put("default", defaultLocale)

		// Carregar todos os locales
		val localesFolder = File(Loritta.LOCALES)
		val prettyGson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
		for (file in localesFolder.listFiles()) {
			if (file.extension == "json" && file.nameWithoutExtension != "default") {
				// Carregar o BaseLocale baseado no locale atual
				val localeAsText = file.readText(Charsets.UTF_8)
				val locale = prettyGson.fromJson(localeAsText, BaseLocale::class.java)
				locale.strings = HashMap<String, String>(defaultLocale.strings) // Clonar strings do default locale
				locales.put(file.nameWithoutExtension, locale)
				// Yay!
			}
		}

		// E agora preencher valores nulos e salvar as traduções
		for ((id, locale) in locales) {
			if (id != "default") {
				val jsonObject = JSON_PARSER.parse(Loritta.GSON.toJson(locale))

				val localeFile = File(Loritta.LOCALES, "$id.json")
				val asJson = JSON_PARSER.parse(localeFile.readText()).obj

				for ((id, obj) in asJson.entrySet()) {
					if (obj.isJsonPrimitive && obj.asJsonPrimitive.isString) {
						locale.strings.put(id, obj.string)
					}
				}

				// Usando Reflection TODO: Remover
				for (field in locale::class.java.declaredFields) {
					if (field.name == "strings") { continue }
					field.isAccessible = true

					val ogValue = field.get(defaultLocale)
					val changedValue = field.get(locale)

					if (changedValue == null || ogValue.equals(changedValue)) {
						field.set(locale, ogValue)
						jsonObject[field.name] = null
						if (ogValue is List<*>) {
							val tree = prettyGson.toJsonTree(ogValue)
							jsonObject["[Translate!]${field.name}"] = tree
						} else {
							jsonObject["[Translate!]${field.name}"] = ogValue
						}
					} else {
						if (changedValue is List<*>) {
							val tree = prettyGson.toJsonTree(changedValue)
							jsonObject[field.name] = tree
						} else {
							jsonObject[field.name] = changedValue
							locale.strings.put(field.name, changedValue as String)
						}
					}
				}

				for ((id, ogValue) in defaultLocale.strings) {
					val changedValue = locale.strings[id]

					if (ogValue.equals(changedValue)) {
						jsonObject["[Translate!]$id"] = ogValue
					} else {
						jsonObject[id] = changedValue
						locale.strings.put(id, changedValue!!)
					}
				}

				File(Loritta.LOCALES, "$id.json").writeText(prettyGson.toJson(jsonObject))
			}
		}

		this.locales.clear()
		this.locales = locales
	}

	fun getLocaleById(localeId: String): BaseLocale {
		return locales.getOrDefault(localeId, locales.get("default"))!!
	}

	@Synchronized
	fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
		val guildId = java.lang.Long.parseLong(guild.getId())
		var musicManager = musicManagers[guildId]

		if (musicManager == null) {
			musicManager = GuildMusicManager(guild, playerManager)
			musicManagers.put(guildId, musicManager)
		}

		guild.audioManager.sendingHandler = musicManager.sendHandler

		return musicManager
	}

	fun checkAndLoad(context: CommandContext, trackUrl: String, override: Boolean = false): Boolean {
		if (!context.handle.voiceState.inVoiceChannel() || context.handle.voiceState.channel.id != context.config.musicConfig.musicGuildId) {
			// Se o cara não estiver no canal de voz ou se não estiver no canal de voz correto...
			val channel = context.guild.getVoiceChannelById(context.config.musicConfig.musicGuildId)

			if (channel != null) {
				context.reply(
						LoriReply(
								context.locale["TOCAR_NOTINCHANNEL", channel.name.stripCodeMarks()],
								Constants.ERROR
						)
				)
			} else {
				context.reply(
						LoriReply(
								context.locale["TOCAR_InvalidChannel"],
								Constants.ERROR
						)
				)
			}
			return false
		}
		loadAndPlay(context, trackUrl, override)
		return true
	}

	val playlistCache = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).maximumSize(100).build<String, AudioPlaylist>().asMap()

	fun loadAndPlay(context: CommandContext, trackUrl: String, override: Boolean = false) {
		loadAndPlay(context, trackUrl, false, override);
	}

	fun loadAndPlay(context: CommandContext, trackUrl: String, alreadyChecked: Boolean, override: Boolean = false) {
		val channel = context.event.channel
		val guild = context.guild
		val musicConfig = context.config.musicConfig
		val musicManager = getGuildAudioPlayer(guild);

		context.guild.audioManager.isSelfMuted = false // Desmutar a Loritta
		context.guild.audioManager.isSelfDeafened = false // E desilenciar a Loritta

		if (playlistCache.containsKey(trackUrl)) {
			playPlaylist(context, musicManager, playlistCache[trackUrl]!!, override)
			return
		}

		playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				if (musicConfig.hasMaxSecondRestriction) { // Se esta guild tem a limitação de áudios...
					if (track.duration > TimeUnit.SECONDS.toMillis(musicConfig.maxSeconds.toLong())) {
						val final = String.format("%02d:%02d", ((musicConfig.maxSeconds / 60) % 60), (musicConfig.maxSeconds % 60));
						channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["MUSIC_MAX", final]).queue();
						return;
					}
				}
				channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale["MUSIC_ADDED", track.info.title.stripCodeMarks().escapeMentions()]).queue()

				play(context, musicManager, AudioTrackWrapper(track, false, context.userHandle, HashMap<String, String>()), override)
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				playlistCache[trackUrl] = playlist
				playPlaylist(context, musicManager, playlist, override)
			}

			override fun noMatches() {
				if (!alreadyChecked) {
					// Ok, não encontramos NADA relacionado a essa música
					// Então vamos pesquisar!
					val items = YouTubeUtils.searchVideosOnYouTube(trackUrl);

					if (items.isNotEmpty()) {
						loadAndPlay(context, items[0].id.videoId, true, override)
						return;
					}
				}
				channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("MUSIC_NOTFOUND", trackUrl)).queue();
			}

			override fun loadFailed(exception: FriendlyException) {
				channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("MUSIC_ERROR", exception.message)).queue();
			}
		})
	}

	fun playPlaylist(context: CommandContext, musicManager: GuildMusicManager, playlist: AudioPlaylist, override: Boolean = false) {
		val channel = context.event.channel
		val musicConfig = context.config.musicConfig

		if (!musicConfig.allowPlaylists && !override) { // Se esta guild NÃO aceita playlists
			var track = playlist.selectedTrack

			if (track == null) {
				track = playlist.tracks[0]
			}

			channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale["MUSIC_ADDED", track.info.title.stripCodeMarks().escapeMentions()]).queue()

			play(context, musicManager, AudioTrackWrapper(track.makeClone(), false, context.userHandle, HashMap<String, String>()), override)
		} else { // Mas se ela aceita...
			var ignored = 0;
			for (track in playlist.tracks) {
				if (musicConfig.hasMaxSecondRestriction) {
					if (track.duration > TimeUnit.SECONDS.toMillis(musicConfig.maxSeconds.toLong())) {
						ignored++;
						continue;
					}
				}

				play(context, musicManager, AudioTrackWrapper(track.makeClone(), false, context.userHandle, HashMap<String, String>()), override);
			}

			if (ignored == 0) {
				channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale["MUSIC_PLAYLIST_ADDED", playlist.tracks.size]).queue()
			} else {
				channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale["MUSIC_PLAYLIST_ADDED_IGNORED", playlist.tracks.size, ignored]).queue()
			}
		}
	}

	fun loadAndPlayNoFeedback(guild: Guild, config: ServerConfig, trackUrl: String) {
		val musicManager = getGuildAudioPlayer(guild);

		if (playlistCache.contains(trackUrl)) {
			val playlist = playlistCache[trackUrl]!!
			loadAndPlayNoFeedback(guild, config, playlist.tracks[Loritta.RANDOM.nextInt(0, playlist.tracks.size)].info.uri)
			return
		}

		playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				play(guild, config, musicManager, AudioTrackWrapper(track, true, guild.selfMember.user, HashMap<String, String>()))
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				playlistCache[trackUrl] = playlist
				loadAndPlayNoFeedback(guild, config, playlist.tracks[Loritta.RANDOM.nextInt(0, playlist.tracks.size)].info.uri)
			}

			override fun noMatches() {
			}

			override fun loadFailed(exception: FriendlyException) {
			}
		})
	}

	fun play(context: CommandContext, musicManager: GuildMusicManager, trackWrapper: AudioTrackWrapper, override: Boolean = false) {
		play(context.guild, context.config, musicManager, trackWrapper, override)
	}

	fun play(guild: Guild, conf: ServerConfig, musicManager: GuildMusicManager, trackWrapper: AudioTrackWrapper, override: Boolean = false) {
		if (musicManager.scheduler.queue.size >= 100)
			return

		val musicGuildId = conf.musicConfig.musicGuildId!!

		if (override) {
			logger.info("Force Playing ${trackWrapper.track.info.title} - in guild ${guild.name} (${guild.id})")
		} else {
			logger.info("Playing ${trackWrapper.track.info.title} - in guild ${guild.name} (${guild.id})")
		}

		connectToVoiceChannel(musicGuildId, guild.audioManager);

		if (override) {
			musicManager.player.startTrack(trackWrapper.track, false)
		} else {
			musicManager.scheduler.queue(trackWrapper, conf)
		}

		LorittaUtilsKotlin.fillTrackMetadata(trackWrapper)
	}

	fun skipTrack(context: CommandContext) {
		val musicManager = getGuildAudioPlayer(context.getGuild());
		musicManager.scheduler.nextTrack();

		context.reply(
				LoriReply(
						context.locale["PULAR_MUSICSKIPPED"],
						"\uD83E\uDD39"
				)
		)
	}

	fun connectToVoiceChannel(id: String, audioManager: AudioManager) {
		if (audioManager.isConnected && audioManager.connectedChannel.id != id) { // Se a Loritta está conectada em um canal de áudio mas não é o que nós queremos...
			audioManager.closeAudioConnection(); // Desconecte do canal atual!
		}

		if (!audioManager.isAttemptingToConnect && audioManager.isConnected && !audioManager.guild.selfMember.voiceState.inVoiceChannel()) {
			// Corrigir bug que simplesmente eu desconecto de um canal de voz magicamente

			// Quando isto acontecer, nós iremos vazar, vlw flw
			audioManager.closeAudioConnection()
		}

		val channels = audioManager.guild.voiceChannels.filter{ it.id == id }

		if (channels.isNotEmpty()) {
			audioManager.openAudioConnection(channels[0])
		}
	}
}