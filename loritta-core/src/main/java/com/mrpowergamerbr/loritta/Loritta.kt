package com.mrpowergamerbr.loritta

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.*
import com.google.common.collect.EvictingQueue
import com.google.common.collect.Queues
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.audio.AudioManager
import com.mrpowergamerbr.loritta.commands.CommandManager
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ProfileSettings
import com.mrpowergamerbr.loritta.listeners.*
import com.mrpowergamerbr.loritta.livestreams.TwitchAPI
import com.mrpowergamerbr.loritta.modules.ServerSupportModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.*
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.threads.RaffleThread
import com.mrpowergamerbr.loritta.threads.RemindersThread
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.tictactoe.TicTacToeServer
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.FanArtConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.fanarts.LorittaFanArt
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.gabriela.GabrielaMessage
import com.mrpowergamerbr.loritta.utils.locale.Gender
import com.mrpowergamerbr.loritta.utils.networkbans.LorittaNetworkBanManager
import com.mrpowergamerbr.loritta.utils.temmieyoutube.TemmieYouTube
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.perfectdreams.loritta.api.platform.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.DiscordEmoteManager
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandManager
import net.perfectdreams.loritta.socket.LorittaSocket
import net.perfectdreams.loritta.socket.network.SocketOpCode
import net.perfectdreams.loritta.socket.network.commands.*
import net.perfectdreams.loritta.tables.Giveaways
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.tables.ReactionOptions
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.NetAddressUtils
import net.perfectdreams.loritta.utils.extensions.obj
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.mercadopago.MercadoPago
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Loritta's main class, where everything (and anything) can happen!
 *
 * @author MrPowerGamerBR
 */
class Loritta(var discordConfig: GeneralDiscordConfig, config: GeneralConfig) : LorittaBot(config) {
	// ===[ STATIC ]===
	companion object {
		// ===[ LORITTA ]===
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

		// ===[ UTILS ]===
		@JvmStatic
		val RANDOM = SplittableRandom() // Um splittable RANDOM global, para não precisar ficar criando vários (menos GC)
		@JvmStatic
		var GSON = Gson() // Gson
		@JvmStatic
		val JSON_PARSER = JsonParser() // Json Parser
		@JvmStatic
		lateinit var youtube: TemmieYouTube // API key do YouTube, usado em alguns comandos

		private val logger = KotlinLogging.logger {}
	}

	// ===[ LORITTA ]===
	// All features!!! :3
	override val supportedFeatures = PlatformFeature.values().toMutableList()

	var lorittaShards = LorittaShards() // Shards da Loritta
	val executor = createThreadPool("Executor Thread %d") // Threads
	val coroutineExecutor = createThreadPool("Coroutine Executor Thread %d")
	val coroutineDispatcher = coroutineExecutor.asCoroutineDispatcher() // Coroutine Dispatcher

	fun createThreadPool(name: String): ExecutorService {
		return Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())
	}

	lateinit var legacyCommandManager: CommandManager // Nosso command manager
	override val commandManager = DiscordCommandManager(this)
	lateinit var dummyServerConfig: MongoServerConfig // Config utilizada em comandos no privado
	var messageInteractionCache = Caffeine.newBuilder().maximumSize(1000L).expireAfterAccess(3L, TimeUnit.MINUTES).build<Long, MessageInteractionFunctions>().asMap()

	var ignoreIds = mutableSetOf<Long>() // IDs para serem ignorados nesta sessão
	val userCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<Long, Long>().asMap()
	val apiCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<String, Long>().asMap()

	// ===[ MONGODB ]===
	lateinit var mongo: MongoClient // MongoDB
	lateinit var serversColl: MongoCollection<MongoServerConfig>
	lateinit var gabrielaMessagesColl: MongoCollection<GabrielaMessage>

	val audioManager: AudioManager

	var youtubeKeys = mutableListOf<String>()
	var lastKeyReset = 0

	lateinit var fanArtConfig: FanArtConfig
	val fanArts: List<LorittaFanArt>
		get() = fanArtConfig.fanArts

	var discordListener = DiscordListener(this) // Vamos usar a mesma instância para todas as shards
	var eventLogListener = EventLogListener(this) // Vamos usar a mesma instância para todas as shards
	var messageListener = MessageListener(this)
	var voiceChannelListener = VoiceChannelListener(this)
	var channelListener = ChannelListener(this)
	var builder: DefaultShardManagerBuilder

	lateinit var raffleThread: RaffleThread
	lateinit var bomDiaECia: BomDiaECia

	var ticTacToeServer = TicTacToeServer()
	var blacklistedServers = mutableMapOf<String, String>()
	val networkBanManager = LorittaNetworkBanManager()

	lateinit var website: LorittaWebsite
	lateinit var websiteThread: Thread

	var twitch = TwitchAPI()
	val connectionManager = ConnectionManager()
	val mercadoPago: MercadoPago
	val socket = LorittaSocket(35575).apply {
		this.registerCommands(
				GetUserByIdCommand(),
				GetUsersByIdCommand(),
				GetGuildByIdCommand(),
				GetGuildsByIdCommand(),
				GetGuildConfigByIdCommand(),
				UpdateGuildConfigByIdCommand(),
				HeartbeatCommand()
		)
	}
	val requestLimiter = RequestLimiter(this)

	init {
		LorittaLauncher.loritta = this
		FOLDER = config.loritta.folders.root
		ASSETS = config.loritta.folders.assets
		TEMP = config.loritta.folders.temp
		LOCALES = config.loritta.folders.locales
		FRONTEND = config.loritta.website.folder
		loadLocales()
		loadLegacyLocales()
		mercadoPago = MercadoPago(
				clientId = config.mercadoPago.clientId,
				clientSecret = config.mercadoPago.clientSecret
		)
		youtube = TemmieYouTube()
		resetYouTubeKeys()
		loadFanArts()
		loadBlacklistedServers()
		Emotes.emoteManager = DiscordEmoteManager()
		Emotes.loadEmotes()
		networkBanManager.loadNetworkBannedUsers()
		GlobalHandler.generateViews()
		audioManager = AudioManager(this)

		val okHttpBuilder = OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS) // O padrão de timeouts é 10 segundos, mas vamos aumentar para evitar problemas.
				.readTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)
				.protocols(listOf(Protocol.HTTP_1_1)) // https://i.imgur.com/FcQljAP.png

		builder = DefaultShardManagerBuilder()
				.setShardsTotal(discordConfig.discord.maxShards)
				.setShards(discordConfig.discord.minShardId, discordConfig.discord.maxShardId)
				.setStatus(discordConfig.discord.status)
				.setToken(discordConfig.discord.clientToken)
				.setBulkDeleteSplittingEnabled(false)
				.setHttpClientBuilder(okHttpBuilder)
				.setDisabledCacheFlags(EnumSet.of(CacheFlag.PRESENCE))
				.addEventListeners(
						discordListener,
						eventLogListener,
						messageListener,
						voiceChannelListener,
						channelListener,
						audioManager.lavalink
				)
				.setVoiceDispatchInterceptor(audioManager.lavalink.voiceInterceptor)
	}

	// Gera uma configuração "dummy" para comandos enviados no privado
	fun generateDummyServerConfig() {
		val dummy = MongoServerConfig("-1").apply { // É usado -1 porque -1 é um número de guild inexistente
			commandPrefix = ""
			mentionOnCommandOutput = false
		}

		dummyServerConfig = dummy
	}

	fun resetYouTubeKeys() {
		youtubeKeys.clear()
		youtubeKeys.addAll(config.youtube.apiKeys)
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
		RestAction.setPassContext(true)

		// Mandar o MongoDB calar a boca
		val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
		val rootLogger = loggerContext.getLogger("org.mongodb.driver")
		rootLogger.level = Level.OFF

		initMongo()
		initPostgreSql()

		generateDummyServerConfig()

		logger.info("Sucesso! Iniciando Loritta (Website)...")

		websiteThread = thread(true, name = "Website Thread") {
			website = LorittaWebsite(config.loritta.website.url, config.loritta.website.folder)
			org.jooby.run({
				website
			})
		}

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		logger.info { "Sucesso! Iniciando Loritta (Discord Bot)..." }

		val shardManager = builder.build()
		lorittaShards.shardManager = shardManager

		if (config.socket.enabled) {
			logger.info { "Sucesso! Iniciando socket client..." }
			socket.connect()
			socket.onSocketConnected = { socketWrapper ->
				socketWrapper.sendRequestAsync(
						SocketOpCode.Discord.IDENTIFY,
						objectNode(
								"lorittaShardId" to config.socket.shardId,
								"lorittaShardName" to config.socket.clientName,
								"discordMaxShards" to discordConfig.discord.maxShards,
								"discordShardMin" to discordConfig.discord.minShardId,
								"discordShardMax" to discordConfig.discord.maxShardId
						),
						success = {
							logger.info("Identification process was a success! We are now identified and ready to send and receive commands!")

							socketWrapper.isReady = true
							socketWrapper.syncDiscordStats()
						},
						failure = {
							logger.error("Failed to identify!")
						}
				)
			}

			socket.onMessageReceived = {
				val socketWrapper = socket.socketWrapper!!

				val uniqueId = UUID.fromString(it["uniqueId"].textValue())
				val request = socketWrapper._requests.getIfPresent(uniqueId)
				socketWrapper._requests.invalidate(uniqueId)

				request?.first?.invoke(it.obj)
			}
		}

		logger.info { "Sucesso! Iniciando comandos e plugins da Loritta..." }

		loadCommandManager() // Inicie todos os comandos da Loritta
		pluginManager.loadPlugins()

		logger.info { "Sucesso! Iniciando threads da Loritta..." }

		logger.info { "Iniciando Livestream Thread..." }
		NewLivestreamThread().start() // Iniciar New Livestream Thread

		logger.info { "Iniciando Update Status Thread..." }
		UpdateStatusThread().start() // Iniciar thread para atualizar o status da Loritta

		logger.info { "Iniciando Tasks..." }
		LorittaTasks.startTasks()

		logger.info { "Iniciando threads de reminders..." }
		RemindersThread().start()

		logger.info { "Iniciando bom dia & cia..." }
		bomDiaECia = BomDiaECia()

		logger.info { "Carregando raffle..." }
		val raffleFile = File(FOLDER, "raffle.json")

		if (raffleFile.exists()) {
			val json = JSON_PARSER.parse(raffleFile.readText()).obj

			RaffleThread.started = json["started"].long
			RaffleThread.lastWinnerId = json["lastWinnerId"].nullString
			RaffleThread.lastWinnerPrize = json["lastWinnerPrize"].nullInt ?: 0
			val userIdArray = json["userIds"].nullArray

			if (userIdArray != null)
				RaffleThread.userIds = GSON.fromJson(userIdArray)
		}

		raffleThread = RaffleThread()
		raffleThread.start()

		DebugLog.startCommandListenerThread()

		GlobalScope.launch(coroutineDispatcher) {
			connectionManager.updateProxies()
		}

		try { ServerSupportModule.loadResponses() } catch (e: FileNotFoundException) {
			logger.error(e) { "Erro ao carregar as respostas automáticas!" }
		}

		// Ou seja, agora a Loritta está funcionando, Yay!
	}

	fun initPostgreSql() {
		logger.info("Iniciando PostgreSQL...")

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					StoredMessages,
					Profiles,
					UserSettings,
					Reminders,
					Reputations,
					UsernameChanges,
					Dailies,
					Marriages,
					RegisterConfigs,
					Mutes,
					Warns,
					GuildProfiles,
					Timers,
					Giveaways,
					ReactionOptions,
					ServerConfigs,
					DonationKeys,
					Payments,
					ShipEffects,
					GitHubIssues,
					ParallaxMetaStorages
			)
		}
	}

	fun initMongo() {
		logger.info("Iniciando MongoDB...")

		val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

		val mongoBuilder = MongoClientOptions.Builder().apply {
			codecRegistry(pojoCodecRegistry)
		}

		val options = mongoBuilder
				.connectionsPerHost(250)
				.build()

		mongo = MongoClient(NetAddressUtils.getWithPortIfMissing(config.mongoDb.address, 27017), options) // Hora de iniciar o MongoClient

		val db = mongo.getDatabase(config.mongoDb.databaseName)

		val dbCodec = db.withCodecRegistry(pojoCodecRegistry)

		serversColl = dbCodec.getCollection("servers", MongoServerConfig::class.java)
		gabrielaMessagesColl = dbCodec.getCollection("gabriela", GabrielaMessage::class.java)
	}

	/**
	 * Loads the server configuration of a guild
	 *
	 * @param guildId the guild's ID
	 * @return        the server configuration
	 * @see           MongoServerConfig
	 */
	fun getServerConfigForGuild(guildId: String): MongoServerConfig {
		val serverConfig = serversColl.find(Filters.eq("_id", guildId)).first()
		return serverConfig ?: MongoServerConfig(guildId)
	}

	/**
	 * Loads the server configuration of a guild
	 *
	 * @param guildId the guild's ID
	 * @return        the server configuration
	 * @see           MongoServerConfig
	 */
	fun getOrCreateServerConfig(guildId: Long): com.mrpowergamerbr.loritta.dao.ServerConfig {
		return transaction(Databases.loritta) {
			com.mrpowergamerbr.loritta.dao.ServerConfig.findById(guildId) ?: com.mrpowergamerbr.loritta.dao.ServerConfig.new(guildId) {

			}
		}
	}

	fun getLorittaProfile(userId: String): Profile? {
		return getLorittaProfile(userId.toLong())
	}

	/**
	 * Loads the profile of an user
	 *
	 * @param userId the user's ID
	 * @return       the user profile
	 * @see          MongoLorittaProfile
	 */
	fun getLorittaProfile(userId: Long): Profile? {
		return transaction(Databases.loritta) {
			Profile.findById(userId)
		}
	}

	fun getOrCreateLorittaProfile(userId: String): Profile {
		return getOrCreateLorittaProfile(userId.toLong())
	}

	fun getActiveMoneyFromDonations(userId: Long): Double {
		return transaction(Databases.loritta) {
			Payment.find {
				(Payments.expiresAt greaterEq System.currentTimeMillis()) and
						(Payments.reason eq PaymentReason.DONATION) and
						(Payments.userId eq userId)
			}.sumByDouble { it.money.toDouble() }
		}
	}

	var idx0 = 0L
	val findProfilePostgre = Queues.synchronizedQueue(EvictingQueue.create<Long>(1000))
	var idx2 = 0L
	val newProfilePostgre = Queues.synchronizedQueue(EvictingQueue.create<Long>(1000))

	fun getOrCreateLorittaProfile(userId: Long): Profile {
		return transaction(Databases.loritta) {
			val start0 = System.nanoTime()
			val sqlProfile = Profile.findById(userId)
			if (idx0 % 100 == 0L) {
				findProfilePostgre.add(System.nanoTime() - start0)
			}
			idx0++

			if (sqlProfile != null) {
				return@transaction sqlProfile
			}

			val start2 = System.nanoTime()
			val newProfile = Profile.new(userId) {
				xp = 0
				isBanned = false
				bannedReason = null
				lastMessageSentAt = 0L
				lastMessageSentHash = 0
				money = 0.0
				isDonator = false
				donatorPaid = 0.0
				donatedAt = 0L
				donationExpiresIn = 0L
				isAfk = false
				settings = ProfileSettings.new {
					gender = Gender.UNKNOWN
					hideSharedServers = false
					hidePreviousUsernames = false
					hideLastSeen = false
					boughtProfiles = arrayOf()
				}
			}
			if (idx2 % 100 == 0L) {
				newProfilePostgre.add(System.nanoTime() - start2)
			}

			idx2++
			return@transaction newProfile
		}
	}

	/**
	 * Initializes the CommandManager
	 *
	 * @see CommandManager
	 */
	fun loadCommandManager() {
		// Isto parece não ter nenhuma utilidade, mas, caso estejamos usando o JRebel, é usado para recarregar o command manager
		// Ou seja, é possível adicionar comandos sem ter que reiniciar tudo!
		legacyCommandManager = CommandManager()
	}

	/**
	 * Loads the Fan Arts from the "fanarts.json" file
	 */
	fun loadFanArts() {
		fanArtConfig = Constants.MAPPER.readValue(File("./fanarts.yml").readText())
	}

	/**
	 * Loads the blacklisted server list from the "blacklisted-servers.json" file
	 */
	fun loadBlacklistedServers() {
		if (File("./blacklisted_servers.json").exists())
			blacklistedServers = GSON.fromJson(File("./blacklisted_servers.json").readText())
	}
}