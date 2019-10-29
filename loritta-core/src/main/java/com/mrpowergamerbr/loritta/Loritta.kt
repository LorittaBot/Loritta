package com.mrpowergamerbr.loritta

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.*
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
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordInstanceConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
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
import net.perfectdreams.loritta.tables.*
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.NetAddressUtils
import net.perfectdreams.loritta.utils.Sponsor
import net.perfectdreams.loritta.utils.extensions.obj
import net.perfectdreams.loritta.utils.extensions.objectNode
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.mercadopago.MercadoPago
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
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
class Loritta(var discordConfig: GeneralDiscordConfig, var discordInstanceConfig: GeneralDiscordInstanceConfig, config: GeneralConfig, instanceConfig: GeneralInstanceConfig) : LorittaBot(config, instanceConfig) {
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

	var discordListener = DiscordListener(this) // Vamos usar a mesma instância para todas as shards
	var eventLogListener = EventLogListener(this) // Vamos usar a mesma instância para todas as shards
	var messageListener = MessageListener(this)
	var voiceChannelListener = VoiceChannelListener(this)
	var channelListener = ChannelListener(this)
	var builder: DefaultShardManagerBuilder

	lateinit var raffleThread: RaffleThread
	lateinit var bomDiaECia: BomDiaECia

	var ticTacToeServer = TicTacToeServer()
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
	var patchData = PatchData()
	var sponsors: List<Sponsor> = listOf()

	init {
		LorittaLauncher.loritta = this
		FOLDER = instanceConfig.loritta.folders.root
		ASSETS = instanceConfig.loritta.folders.assets
		TEMP = instanceConfig.loritta.folders.temp
		LOCALES = instanceConfig.loritta.folders.locales
		FRONTEND = instanceConfig.loritta.website.folder
		loadLocales()
		loadLegacyLocales()
		mercadoPago = MercadoPago(
				clientId = config.mercadoPago.clientId,
				clientSecret = config.mercadoPago.clientSecret
		)
		youtube = TemmieYouTube()
		resetYouTubeKeys()
		loadFanArts()
		Emotes.emoteManager = DiscordEmoteManager()
		Emotes.loadEmotes()
		GlobalHandler.generateViews()
		audioManager = AudioManager(this)

		net.perfectdreams.loritta.website.LorittaWebsite.init() // hack!

		val dispatcher = Dispatcher()
		dispatcher.maxRequestsPerHost = discordConfig.discord.maxRequestsPerHost

		val okHttpBuilder = OkHttpClient.Builder()
				.dispatcher(dispatcher)
				.connectTimeout(discordConfig.okHttp.connectTimeout, TimeUnit.SECONDS) // O padrão de timeouts é 10 segundos, mas vamos aumentar para evitar problemas.
				.readTimeout(discordConfig.okHttp.readTimeout, TimeUnit.SECONDS)
				.writeTimeout(discordConfig.okHttp.writeTimeout, TimeUnit.SECONDS)
				.protocols(listOf(Protocol.HTTP_1_1)) // https://i.imgur.com/FcQljAP.png

		builder = DefaultShardManagerBuilder()
				.setShardsTotal(discordConfig.discord.maxShards)
				.setShards(discordInstanceConfig.discord.minShardId, discordInstanceConfig.discord.maxShardId)
				.setStatus(discordConfig.discord.status)
				.setToken(discordConfig.discord.clientToken)
				.setBulkDeleteSplittingEnabled(false)
				.setHttpClientBuilder(okHttpBuilder)
				.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY))
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

	val isMaster: Boolean
		get() {
			return loritta.instanceConfig.loritta.currentClusterId == 1L
		}

	val lorittaCluster: GeneralConfig.LorittaClusterConfig
		get() {
			return loritta.config.clusters.first { it.id == loritta.instanceConfig.loritta.currentClusterId }
		}

	val lorittaInternalApiKey: GeneralConfig.LorittaConfig.WebsiteConfig.AuthenticationKey
		get() {
			return loritta.config.loritta.website.apiKeys.first { it.description == "Loritta Internal Key" }
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

		if (File("./blacklisted_servers.json").exists()) {
			logger.info { "Migrating guild bans to the database..." }
			val blacklistedServers = Loritta.GSON.fromJson<Map<String, String>>(File("./blacklisted_servers.json").readText())

			for ((id, reason) in blacklistedServers) {
				transaction(Databases.loritta) {
					BlacklistedGuilds.insert {
						it[BlacklistedGuilds.id] = EntityID(id.toLong(), BlacklistedUsers)
						it[bannedAt] = System.currentTimeMillis()
						it[BlacklistedGuilds.reason] = reason
					}
				}
			}

			File("./blacklisted_servers.json").delete()
		}

		networkBanManager.migrateNetworkBannedUsers()

		logger.info("Sucesso! Iniciando Loritta (Website)...")

		websiteThread = thread(true, name = "Website Thread") {
			website = LorittaWebsite(instanceConfig.loritta.website.url, instanceConfig.loritta.website.folder)
			net.perfectdreams.loritta.website.LorittaWebsite.INSTANCE.blog.posts = net.perfectdreams.loritta.website.LorittaWebsite.INSTANCE.blog.loadAllBlogPosts()

			org.jooby.run({
				website
			})
		}

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		logger.info { "Sucesso! Iniciando Loritta (Discord Bot)..." }

		val shardManager = builder.build()
		lorittaShards.shardManager = shardManager

		generateDummyServerConfig()

		if (config.socket.enabled) {
			logger.info { "Sucesso! Iniciando socket client..." }
			socket.connect()
			socket.onSocketConnected = { socketWrapper ->
				socketWrapper.sendRequestAsync(
						SocketOpCode.Discord.IDENTIFY,
						objectNode(
								"lorittaShardId" to config.socket.shardId,
								"lorittaShardName" to config.socket.clientName
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
					ParallaxMetaStorages,
					BotVotes,
					StoredMessages,
					StarboardMessages,
					Sponsors,
					EconomyConfigs,
					ExecutedCommandsLog,
					BlacklistedUsers,
					BlacklistedGuilds
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

	fun getOrCreateLorittaProfile(userId: Long): Profile {
		return transaction(Databases.loritta) {
			val sqlProfile = Profile.findById(userId)

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
}