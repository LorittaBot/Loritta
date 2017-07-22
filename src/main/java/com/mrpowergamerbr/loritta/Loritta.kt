package com.mrpowergamerbr.loritta

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.salomonbrys.kotson.fromJson
import com.google.common.cache.CacheBuilder
import com.google.gson.Gson
import com.mongodb.MongoClient
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.CommandManager
import com.mrpowergamerbr.loritta.frontend.LorittaWebsite
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.amino.AminoRepostThread
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.config.ServerFanClub
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.USLocale
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
	val executor = Executors.newScheduledThreadPool(32) // Threads
	lateinit var commandManager: CommandManager // Nosso command manager
	lateinit var dummyServerConfig: ServerConfig // Config utilizada em comandos no privado
	var messageContextCache = CacheBuilder.newBuilder().maximumSize(1000L).expireAfterAccess(5L, TimeUnit.MINUTES).build<Any, Any>().asMap()
	var serversFanClub = listOf<ServerFanClub>()
	var locales = mutableMapOf<String, BaseLocale>()

	// ===[ MONGODB ]===
	lateinit var mongo: MongoClient // MongoDB
	lateinit var ds: Datastore // MongoDB²
	lateinit var morphia: Morphia// MongoDB³

	// ===[ UTILS ]===
	var hal = JMegaHal() // JMegaHal, usado nos comandos de frase tosca

	// ===[ MÚSICA ]===
	lateinit var playerManager: AudioPlayerManager
	lateinit var musicManagers: MutableMap<Long, GuildMusicManager>

	// Constructor da Loritta
	constructor(config: LorittaConfig) {
		Loritta.config = config // Salvar a nossa configuração na variável Loritta#config
		loadServersFromFanClub()
		loadLocales()
		Loritta.temmieMercadoPago = TemmieMercadoPago(config.mercadoPagoClientId, config.mercadoPagoClientToken) // Iniciar o client do MercadoPago
		Loritta.youtube = TemmieYouTube(config.youtubeKey)
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

		mongo = MongoClient() // Hora de iniciar o MongoClient
		morphia = Morphia() // E o Morphia
		ds = morphia.createDatastore(mongo, "loritta") // E também crie uma datastore (tudo da Loritta será salvo na database "loritta")
		generateDummyServerConfig()

		println("Sucesso! Iniciando Loritta (Discord Bot)...") // Agora iremos iniciar o bot

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		val generateShards = Loritta.config.shards - 1

		val okHttpBuilder = OkHttpClient.Builder()
				.connectTimeout(25, TimeUnit.SECONDS)
				.readTimeout(25, TimeUnit.SECONDS)
				.writeTimeout(25, TimeUnit.SECONDS)
		for (idx in 0..generateShards) {
			println("Iniciando Shard $idx...")
			val shard = JDABuilder(AccountType.BOT)
					.useSharding(idx, Loritta.config.shards)
					.setToken(Loritta.config.clientToken)
					.setHttpClientBuilder(okHttpBuilder)
					.buildBlocking();
			lorittaShards.shards.add(shard)
		}

		loadCommandManager() // Inicie todos os comandos da Loritta

		println("Sucesso! Iniciando Loritta (Website)...") // E agora iremos iniciar o website da Loritta
		val website = { LorittaWebsite.init(config.websiteUrl, config.frontendFolder) }
		Thread(website, "Website Thread").start()
		println("Sucesso! Iniciando threads da Loritta...")

		AminoRepostThread().start() // Iniciar Amino Repost Thread

		NewYouTubeVideosThread().start() // Iniciar New YouTube Videos Thread

		NewRssFeedThread().start() // Iniciar Feed Rss

		UpdateStatusThread().start() // Iniciar thread para atualizar o status da Loritta

		DiscordBotsInfoThread().start() // Iniciar thread para atualizar os servidores no Discord Bots

		LorittaUtils.startNotMigratedYetThreads() // Iniciar threads que não foram migradas para Kotlin

		// Iniciar coisas musicais
		musicManagers = HashMap()
		playerManager = DefaultAudioPlayerManager()

		AudioSourceManagers.registerRemoteSources(playerManager)
		AudioSourceManagers.registerLocalSource(playerManager)

		val discordListener = DiscordListener(this); // Vamos usar a mesma instância para todas as shards
		val eventLogListener = EventLogListener(this); // Vamos usar a mesma instância para todas as shards

		// Vamos registrar o nosso event listener em todas as shards!
		for (jda in lorittaShards.shards) {
			jda.addEventListener(discordListener) // Hora de registrar o nosso listener
			jda.addEventListener(eventLogListener) // E o nosso outro listener também!
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
		val doc = mongo.getDatabase("loritta").getCollection("servers").find(Filters.eq("_id", guildId)).first();
		if (doc != null) {
			val config = ds.get(ServerConfig::class.java, doc.get("_id"));
			return config;
		} else {
			return ServerConfig().apply { this.guildId = guildId }
		}
	}

	/**
	 * Carrega um LorittaProfile de um usuário
	 *
	 * @param userId
	 * @return LorittaProfile
	 */
	fun getLorittaProfileForUser(userId: String): LorittaProfile {
		val doc = mongo.getDatabase("loritta").getCollection("users").find(Filters.eq("_id", userId)).first();
		if (doc != null) {
			val profile = ds.get(LorittaProfile::class.java, doc.get("_id"));
			return profile;
		} else {
			return LorittaProfile(userId);
		}
	}

	/**
	 * Cria o CommandManager
	 */
	fun loadCommandManager() {
		// Isto parece não ter nenhuma utilidade, mas, caso estejamos usando o JRebel, é usado para recarregar o command manager
		// Ou seja, é possível adicionar comandos sem ter que reiniciar tudo!
		commandManager = CommandManager()
	}

	/**
	 * Carrega todos os servidores do Fã Clube da Loritta
	 */
	fun loadServersFromFanClub() {
		serversFanClub = gson.fromJson<List<ServerFanClub>>(File("./fanclub.json").readText())
	}

	/**
	 * Inicia os locales da Loritta
	 */
	fun loadLocales() {
		locales.put("us", USLocale())
		locales.put("default", BaseLocale())
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

		guild.getAudioManager().setSendingHandler(musicManager.sendHandler)

		return musicManager
	}

	fun loadAndPlay(context: CommandContext, trackUrl: String) {
		loadAndPlay(context, trackUrl, false);
	}

	fun loadAndPlay(context: CommandContext, trackUrl: String, alreadyChecked: Boolean) {
		val channel = context.event.channel
		val guild = context.guild
		val musicConfig = context.config.musicConfig
		val musicManager = getGuildAudioPlayer(guild);

		playerManager.loadItemOrdered(musicManager, trackUrl, object: AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				if (musicConfig.hasMaxSecondRestriction) { // Se esta guild tem a limitação de áudios...
					if (track.getDuration() > TimeUnit.SECONDS.toMillis(musicConfig.maxSeconds.toLong())) {
						var final = String.format("%02d:%02d", ((musicConfig.maxSeconds / 60) % 60), (musicConfig.maxSeconds % 60));
						channel.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.MUSIC_MAX.msgFormat(final)).queue();
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
					for (track in playlist.getTracks()) {
						if (musicConfig.hasMaxSecondRestriction) {
							if (track.duration > TimeUnit.SECONDS.toMillis(musicConfig.maxSeconds.toLong())) {
								ignored++;
								continue;
							}
						}

						play(context, musicManager,
								AudioTrackWrapper(track, false, context.userHandle, HashMap<String, String>()));
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
				channel.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.MUSIC_NOTFOUND.msgFormat(trackUrl)).queue();
			}

			override fun loadFailed(exception: FriendlyException) {
				channel.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.MUSIC_ERROR.msgFormat(exception.message)).queue();
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
				play(guild, config, musicManager, AudioTrackWrapper(playlist.tracks[Loritta.random.nextInt(0, playlist.tracks.size)], true, guild.selfMember.user, HashMap<String, String>()))
			}

			override fun noMatches() {
				if (musicConfig.urls.contains(trackUrl)) {
					musicConfig.urls.remove(trackUrl);
					ds.save(config);
				}
			}

			override fun loadFailed(exception: FriendlyException) {
				if (musicConfig.urls.contains(trackUrl)) {
					musicConfig.urls.remove(trackUrl);
					ds.save(config);
				}
			}
		})
	}

	fun play(context: CommandContext, musicManager: GuildMusicManager, trackWrapper: AudioTrackWrapper) {
		play(context.guild, context.config, musicManager, trackWrapper)
	}

	fun play(guild: Guild, conf: ServerConfig, musicManager: GuildMusicManager, trackWrapper: AudioTrackWrapper) {
		val musicGuildId = conf.musicConfig.musicGuildId!!

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

		val channels = audioManager.guild.voiceChannels.filter{ it.id == id }
		if (channels.isNotEmpty()) {
			audioManager.openAudioConnection(channels[0])
		}
	}
}