package com.mrpowergamerbr.loritta

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.common.cache.CacheBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.CommandManager
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.listeners.MusicMessageListener
import com.mrpowergamerbr.loritta.threads.AminoRepostThread
import com.mrpowergamerbr.loritta.threads.DiscordBotsInfoThread
import com.mrpowergamerbr.loritta.threads.FetchFacebookPostsThread
import com.mrpowergamerbr.loritta.threads.NewRssFeedThread
import com.mrpowergamerbr.loritta.threads.NewYouTubeVideosThread
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.FacebookPostWrapper
import com.mrpowergamerbr.loritta.utils.LorittaShards
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.ServerFanClubEntry
import com.mrpowergamerbr.loritta.utils.YouTubeUtils
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.config.ServerFanClub
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
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
import okhttp3.OkHttpClient
import org.jibble.jmegahal.JMegaHal
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.Morphia
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

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
		val FOLDER = "/home/servers/loritta/assets/" // Pasta usada na Loritta
		@JvmField
		val TEMP = "/home/servers/loritta/temp/" // Pasta usada para coisas temporarias
		@JvmField
		val LOCALES = "/home/servers/loritta/locales/" // Pasta usada para as locales
		@JvmStatic
		var temmieMercadoPago: TemmieMercadoPago? = null // Usado na página de "doar"

		// ===[ UTILS ]===
		@JvmStatic
		val random = SplittableRandom() // Um splittable random global, para não precisar ficar criando vários (menos GC)
		@JvmStatic
		val gson = Gson() // Gson
		@JvmStatic
		lateinit var youtube: TemmieYouTube // API key do YouTube, usado em alguns comandos
	}
	// ===[ LORITTA ]===
	var lorittaShards = LorittaShards() // Shards da Loritta
	val executor = Executors.newScheduledThreadPool(64) // Threads
	lateinit var commandManager: CommandManager // Nosso command manager
	lateinit var dummyServerConfig: ServerConfig // Config utilizada em comandos no privado
	var messageContextCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterAccess(5L, TimeUnit.MINUTES).build<Any, Any>().asMap()
	var rawServersFanClub = listOf<ServerFanClub>()
	var serversFanClub = mutableListOf<ServerFanClubEntry>()
	var locales = mutableMapOf<String, BaseLocale>()
	var ignoreIds = mutableListOf<String>() // IDs para serem ignorados nesta sessão
	val userCooldown = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.MINUTES).maximumSize(100).build<Any, Any>().asMap()

	var southAmericaMemesPageCache = mutableListOf<FacebookPostWrapper>()
	var southAmericaMemesGroupCache = mutableListOf<FacebookPostWrapper>()
	var memeguy1997PageCache = mutableListOf<FacebookPostWrapper>()
	var memeguy1997GroupCache = mutableListOf<FacebookPostWrapper>()

	// ===[ MONGODB ]===
	lateinit var mongo: MongoClient // MongoDB
	lateinit var ds: Datastore // MongoDB²
	lateinit var morphia: Morphia// MongoDB³

	// ===[ UTILS ]===
	var hal = JMegaHal() // JMegaHal, usado nos comandos de frase tosca

	// ===[ MÚSICA ]===
	lateinit var playerManager: AudioPlayerManager
	lateinit var musicManagers: MutableMap<Long, GuildMusicManager>
	var songThrottle = mutableMapOf<String, Long>()

	var isMusicOnly: Boolean = false

	// Constructor da Loritta
	constructor(config: LorittaConfig, isMusicOnly: Boolean) {
		Loritta.config = config // Salvar a nossa configuração na variável Loritta#config
		loadLocales()
		Loritta.temmieMercadoPago = TemmieMercadoPago(config.mercadoPagoClientId, config.mercadoPagoClientToken) // Iniciar o client do MercadoPago
		Loritta.youtube = TemmieYouTube(config.youtubeKey)
		this.isMusicOnly = isMusicOnly
	}

	// Gera uma configuração "dummy" para comandos enviados no privado
	fun generateDummyServerConfig() {
		val dummy = ServerConfig().apply {
			guildId = "-1" // É usado -1 porque -1 é um número de guild inexistente
			commandPrefix = ""
		}

		dummyServerConfig = dummy;
	}

	// Inicia a Loritta
	fun start() {
		// Mandar o MongoDB calar a boca
		val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
		val rootLogger = loggerContext.getLogger("org.mongodb.driver")
		rootLogger.level = Level.OFF

		println("Iniciando MongoDB...")

		val builder = MongoClientOptions.Builder().apply {
			connectionsPerHost(2000);
		}
		val options = builder.build()

		mongo = MongoClient("127.0.0.1:27017", options) // Hora de iniciar o MongoClient
		morphia = Morphia() // E o Morphia
		ds = morphia.createDatastore(mongo, "loritta") // E também crie uma datastore (tudo da Loritta será salvo na database "loritta")
		generateDummyServerConfig()

		println("Sucesso! Iniciando Loritta (Discord Bot)...") // Agora iremos iniciar o bot

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		val generateShards = Loritta.config.shards - 1

		val okHttpBuilder = OkHttpClient.Builder()
				.connectTimeout(60, TimeUnit.SECONDS)
				.readTimeout(60, TimeUnit.SECONDS)
				.writeTimeout(60, TimeUnit.SECONDS)

		for (idx in 0..generateShards) {
			println("Iniciando Shard $idx...")
			val shard = JDABuilder(AccountType.BOT)
					.useSharding(idx, Loritta.config.shards)
					.setToken(Loritta.config.clientToken)
					.setHttpClientBuilder(okHttpBuilder)
					.buildBlocking()
			lorittaShards.shards.add(shard)
		}

		loadCommandManager() // Inicie todos os comandos da Loritta
		loadServersFromFanClub() // Carregue todos os servidores do fã clube da Loritta

		if (!isMusicOnly) {
			println("Sucesso! Iniciando Loritta (Website)...") // E agora iremos iniciar o website da Loritta
			val website = { LorittaWebsite.init(config.websiteUrl, config.frontendFolder) }
			Thread(website, "Website Thread").start()
			println("Sucesso! Iniciando threads da Loritta...")

			AminoRepostThread().start() // Iniciar Amino Repost Thread

			NewYouTubeVideosThread().start() // Iniciar New YouTube Videos Thread

			NewRssFeedThread().start() // Iniciar Feed Rss

			UpdateStatusThread().start() // Iniciar thread para atualizar o status da Loritta

			DiscordBotsInfoThread().start() // Iniciar thread para atualizar os servidores no Discord Bots

			FetchFacebookPostsThread().start() // Iniciar thread para pegar posts do Facebook

			LorittaUtils.startNotMigratedYetThreads() // Iniciar threads que não foram migradas para Kotlin

			val discordListener = DiscordListener(this); // Vamos usar a mesma instância para todas as shards
			val eventLogListener = EventLogListener(this); // Vamos usar a mesma instância para todas as shards

			// Vamos registrar o nosso event listener em todas as shards!
			for (jda in lorittaShards.shards) {
				jda.addEventListener(discordListener) // Hora de registrar o nosso listener
				jda.addEventListener(eventLogListener) // E o nosso outro listener também!
			}
		} else {
			// Iniciar coisas musicais
			musicManagers = HashMap()
			playerManager = DefaultAudioPlayerManager()

			val trackInfoExecutorServiceField = playerManager::class.java.getDeclaredField("trackInfoExecutorService")

			trackInfoExecutorServiceField.isAccessible = true
			val trackInfoExecutorService = trackInfoExecutorServiceField.get(playerManager) as ThreadPoolExecutor
			trackInfoExecutorService.maximumPoolSize = 100

			AudioSourceManagers.registerRemoteSources(playerManager)
			AudioSourceManagers.registerLocalSource(playerManager)

			LorittaUtils.startAutoPlaylist()

			val messageListener = MusicMessageListener(this)

			for (jda in lorittaShards.shards) {
				jda.addEventListener(messageListener) // Hora de registrar o nosso listener de somente receber comandos de música
			}
		}
		// Ou seja, agora a Loritta está funcionando, Yay!
	}

	/**
	 * Carrega um ServerConfig de uma guild
	 *
	 * @param guildId
	 * @return ServerConfig
	 */
	fun getServerConfigForGuild(guildId: String): ServerConfig {
		val serverConfig = ds.createQuery(ServerConfig::class.java).field("_id").equal(guildId).get()
		return serverConfig ?: ServerConfig().apply { this.guildId = guildId }
	}

	/**
	 * Carrega um LorittaProfile de um usuário
	 *
	 * @param userId
	 * @return LorittaProfile
	 */
	fun getLorittaProfileForUser(userId: String): LorittaProfile {
		val userProfile = ds.createQuery(LorittaProfile::class.java).field("_id").equal(userId).get()
		return userProfile ?: LorittaProfile(userId)
	}

	/**
	 * Cria o CommandManager
	 */
	fun loadCommandManager() {
		// Isto parece não ter nenhuma utilidade, mas, caso estejamos usando o JRebel, é usado para recarregar o command manager
		// Ou seja, é possível adicionar comandos sem ter que reiniciar tudo!
		commandManager = CommandManager(isMusicOnly)
	}

	/**
	 * Carrega todos os servidores do Fã Clube da Loritta
	 */
	fun loadServersFromFanClub() {
		rawServersFanClub = gson.fromJson<List<ServerFanClub>>(File("./fanclub.json").readText())
		LorittaUtilsKotlin.generateServersInFanClub()
	}

	/**
	 * Inicia os locales da Loritta
	 */
	fun loadLocales() {
		val locales = mutableMapOf<String, BaseLocale>()

		// Carregar primeiro o locale padrão
		val defaultLocaleFile = File(Loritta.LOCALES, "default.json")
		val localeAsText = defaultLocaleFile.readText(Charsets.UTF_8)
		val defaultLocale = gson.fromJson(localeAsText, BaseLocale::class.java) // Carregar locale do jeito velho
		val defaultJsonLocale = JsonParser().parse(localeAsText).obj // Mas também parsear como JSON

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
				val jsonObject = JsonParser().parse(Loritta.gson.toJson(locale))

				val localeFile = File(Loritta.LOCALES, "$id.json")
				val asJson = JsonParser().parse(localeFile.readText()).obj

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

	fun checkAndLoad(context: CommandContext, trackUrl: String): Boolean {
		if (!context.handle.voiceState.inVoiceChannel() || context.handle.voiceState.channel.id != context.config.musicConfig.musicGuildId) {
			// Se o cara não estiver no canal de voz ou se não estiver no canal de voz correto...
			context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.TOCAR_NOTINCHANNEL.msgFormat())
			return false
		}
		loadAndPlay(context, trackUrl)
		return true
	}

	fun loadAndPlay(context: CommandContext, trackUrl: String) {
		loadAndPlay(context, trackUrl, false);
	}

	fun loadAndPlay(context: CommandContext, trackUrl: String, alreadyChecked: Boolean) {
		val channel = context.event.channel
		val guild = context.guild
		val musicConfig = context.config.musicConfig
		val musicManager = getGuildAudioPlayer(guild);

		context.guild.audioManager.isSelfMuted = false // Desmutar a Loritta
		context.guild.audioManager.isSelfDeafened = false // E desilenciar a Loritta

		playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				if (musicConfig.hasMaxSecondRestriction) { // Se esta guild tem a limitação de áudios...
					if (track.duration > TimeUnit.SECONDS.toMillis(musicConfig.maxSeconds.toLong())) {
						var final = String.format("%02d:%02d", ((musicConfig.maxSeconds / 60) % 60), (musicConfig.maxSeconds % 60));
						channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.MUSIC_MAX.msgFormat(final)).queue();
						return;
					}
				}
				channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale.MUSIC_ADDED.msgFormat(track.info.title)).queue()

				play(context, musicManager, AudioTrackWrapper(track, false, context.userHandle, HashMap<String, String>()))
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				if (!musicConfig.allowPlaylists) { // Se esta guild NÃO aceita playlists
					var track = playlist.selectedTrack

					if (track == null) {
						track = playlist.tracks[0]
					}

					channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale.MUSIC_ADDED.msgFormat(track.info.title)).queue()

					play(context, musicManager, AudioTrackWrapper(track, false, context.userHandle, HashMap<String, String>()))
				} else { // Mas se ela aceita...
					var ignored = 0;
					for (track in playlist.tracks) {
						if (musicConfig.hasMaxSecondRestriction) {
							if (track.duration > TimeUnit.SECONDS.toMillis(musicConfig.maxSeconds.toLong())) {
								ignored++;
								continue;
							}
						}

						play(context, musicManager, AudioTrackWrapper(track, false, context.userHandle, HashMap<String, String>()));
					}

					if (ignored == 0) {
						channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale.MUSIC_PLAYLIST_ADDED.msgFormat(playlist.tracks.size)).queue()
					} else {
						channel.sendMessage("\uD83D\uDCBD **|** " + context.getAsMention(true) + context.locale.MUSIC_PLAYLIST_ADDED_IGNORED.msgFormat(playlist.tracks.size, ignored)).queue()
					}
				}
			}

			override fun noMatches() {
				if (!alreadyChecked) {
					// Ok, não encontramos NADA relacionado a essa música
					// Então vamos pesquisar!
					val items = YouTubeUtils.searchVideosOnYouTube(trackUrl);

					if (items.isNotEmpty()) {
						loadAndPlay(context, items[0].id.videoId, true);
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

	fun loadAndPlayNoFeedback(guild: Guild, config: ServerConfig, trackUrl: String) {
		val musicConfig = config.musicConfig
		val musicManager = getGuildAudioPlayer(guild);

		playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				play(guild, config, musicManager, AudioTrackWrapper(track, true, guild.selfMember.user, HashMap<String, String>()))
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				loadAndPlayNoFeedback(guild, config, playlist.tracks[Loritta.random.nextInt(0, playlist.tracks.size)].info.uri)
			}

			override fun noMatches() {
			}

			override fun loadFailed(exception: FriendlyException) {
			}
		})
	}

	fun play(context: CommandContext, musicManager: GuildMusicManager, trackWrapper: AudioTrackWrapper) {
		play(context.guild, context.config, musicManager, trackWrapper)
	}

	fun play(guild: Guild, conf: ServerConfig, musicManager: GuildMusicManager, trackWrapper: AudioTrackWrapper) {
		val musicGuildId = conf.musicConfig.musicGuildId!!

		println("Playing ${trackWrapper.track.info.title} - in guild ${guild.name}! (State: ${guild.audioManager.isConnected}")

		connectToVoiceChannel(musicGuildId, guild.audioManager);

		musicManager.scheduler.queue(trackWrapper);

		LorittaUtilsKotlin.fillTrackMetadata(trackWrapper);
	}

	fun skipTrack(context: CommandContext) {
		val musicManager = getGuildAudioPlayer(context.getGuild());
		musicManager.scheduler.nextTrack();
		val channel = context.event.channel
		channel.sendMessage("🤹 ${context.locale.PULAR_MUSICSKIPPED.msgFormat()}").queue();
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