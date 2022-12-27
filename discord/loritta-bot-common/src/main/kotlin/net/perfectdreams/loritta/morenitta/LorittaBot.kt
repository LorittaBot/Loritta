package net.perfectdreams.loritta.morenitta

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.*
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.ratelimit.ParallelRequestRateLimiter
import dev.kord.rest.request.KtorRequestException
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.request.StackTraceRecoveringKtorRequestHandler
import dev.kord.rest.service.RestClient
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.perfectdreams.loritta.morenitta.commands.CommandManager
import net.perfectdreams.loritta.morenitta.listeners.*
import net.perfectdreams.loritta.morenitta.tables.*
import net.perfectdreams.loritta.morenitta.tables.Dailies
import net.perfectdreams.loritta.morenitta.tables.Marriages
import net.perfectdreams.loritta.morenitta.tables.Profiles
import net.perfectdreams.loritta.morenitta.tables.ShipEffects
import net.perfectdreams.loritta.morenitta.tables.StarboardMessages
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.morenitta.threads.RaffleThread
import net.perfectdreams.loritta.morenitta.threads.RemindersThread
import net.perfectdreams.loritta.morenitta.utils.*
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.GuildImpl
import net.perfectdreams.discordinteraktions.common.DiscordInteraKTions
import net.perfectdreams.dreamstorageservice.client.DreamStorageServiceClient
import net.perfectdreams.exposedpowerutils.sql.createOrUpdatePostgreSQLEnum
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.discord.gateway.modules.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionsManager
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.CommandMentions
import net.perfectdreams.loritta.cinnamon.discord.utils.*
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosPackageInfoUpdater
import net.perfectdreams.loritta.cinnamon.discord.utils.dailytax.DailyTaxCollector
import net.perfectdreams.loritta.cinnamon.discord.utils.dailytax.DailyTaxWarner
import net.perfectdreams.loritta.cinnamon.discord.utils.directmessageprocessor.PendingImportantNotificationsProcessor
import net.perfectdreams.loritta.morenitta.utils.ecb.ECBManager
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.DiscordCacheService
import net.perfectdreams.loritta.cinnamon.discord.utils.falatron.Falatron
import net.perfectdreams.loritta.cinnamon.discord.utils.falatron.FalatronModelsManager
import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleVisionOCRClient
import net.perfectdreams.loritta.cinnamon.discord.utils.google.HackyGoogleTranslateClient
import net.perfectdreams.loritta.cinnamon.discord.utils.images.EmojiImageCache
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.DiscordGatewayEventsProcessorMetrics
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.PrometheusPushClient
import net.perfectdreams.loritta.cinnamon.discord.utils.soundboard.Soundboard
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnectionManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Background
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundStorageType
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Drops
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.CollectedChristmas2022Points
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.Christmas2022SonhosTransactionsLog
import net.perfectdreams.loritta.common.exposed.tables.CachedDiscordWebhooks
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentGenre
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentType
import net.perfectdreams.loritta.morenitta.platform.discord.DiscordEmoteManager
import net.perfectdreams.loritta.morenitta.platform.discord.utils.BucketedController
import net.perfectdreams.loritta.morenitta.platform.discord.utils.RateLimitChecker
import net.perfectdreams.loritta.morenitta.tables.BannedUsers
import net.perfectdreams.loritta.morenitta.tables.CachedDiscordUsers
import net.perfectdreams.loritta.morenitta.tables.Payments
import net.perfectdreams.loritta.morenitta.tables.SonhosBundles
import net.perfectdreams.loritta.morenitta.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.morenitta.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs.*
import net.perfectdreams.loritta.morenitta.twitch.TwitchAPI
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.MediaTypeUtils
import net.perfectdreams.loritta.common.utils.StoragePaths
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.morenitta.analytics.stats.LorittaStatsCollector
import net.perfectdreams.loritta.morenitta.christmas2022event.LorittaChristmas2022Event
import net.perfectdreams.loritta.morenitta.christmas2022event.listeners.ReactionListener
import net.perfectdreams.loritta.morenitta.dao.*
import net.perfectdreams.loritta.morenitta.interactions.InteractivityManager
import net.perfectdreams.loritta.morenitta.modules.WelcomeModule
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandMap
import net.perfectdreams.loritta.morenitta.platform.discord.utils.JVMLorittaAssets
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.utils.BomDiaECia
import net.perfectdreams.loritta.morenitta.utils.Sponsor
import net.perfectdreams.loritta.morenitta.utils.TrinketsStuff
import net.perfectdreams.loritta.morenitta.utils.config.*
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import net.perfectdreams.loritta.morenitta.utils.giveaway.GiveawayManager
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.utils.metrics.Prometheus
import net.perfectdreams.loritta.morenitta.utils.devious.DeviousConverter
import net.perfectdreams.loritta.morenitta.utils.devious.GatewaySessionData
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.payments.PaymentReason
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.SpicyMorenittaBundle
import net.perfectdreams.loritta.morenitta.website.SpicyMorenittaDevelopmentBundle
import net.perfectdreams.loritta.morenitta.website.SpicyMorenittaProductionBundle
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.jetbrains.exposed.sql.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.reflect.Modifier
import java.nio.file.*
import java.security.SecureRandom
import java.sql.Connection
import java.time.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.io.path.*
import kotlin.math.ceil
import kotlin.reflect.KClass
import kotlin.time.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Loritta's main class, where everything (and anything) can happen!
 *
 * @author MrPowerGamerBR
 */
class LorittaBot(
	val clusterId: Int,
	val config: BaseConfig,
	val languageManager: LanguageManager,
	val localeManager: LocaleManager,
	val pudding: Pudding,
	val cacheFolder: File,
	val initialSessions: Map<Int, GatewaySessionData>
) {
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
		var FRONTEND = "/home/servers/loritta/frontend/" // Pasta usada para o frontend

		// ===[ UTILS ]===
		@JvmStatic
		val RANDOM = SplittableRandom() // Um splittable RANDOM global, para não precisar ficar criando vários (menos GC)
		@JvmStatic
		var GSON = Gson() // Gson

		private val logger = KotlinLogging.logger {}

		// We multiply by 8 because... uuuh, sometimes threads get stuck due to dumb stuff that we need to fix.
		val MESSAGE_EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors() * 8
	}

	@OptIn(KordUnsafe::class)
	val rest = RestClient(
		BetterSTRecoveringKtorRequestHandler(
			KtorRequestHandler(
				config.loritta.discord.token,
				// By default, Kord uses ExclusionRequestRateLimiter, and that suspends all coroutines if a request is ratelimited
				// So we need to use the ParallelRequestRateLimiter
				requestRateLimiter = ParallelRequestRateLimiter()
			)
		)
	)

	@OptIn(KordExperimental::class)
	val kord = Kord.restOnly(config.loritta.discord.token) {
		requestHandler {
			StackTraceRecoveringKtorRequestHandler(KtorRequestHandler(it.token))
		}
	}

	val cache = DiscordCacheService(this)

	val interaKTions = DiscordInteraKTions(
		kord,
		config.loritta.discord.applicationId
	)

	val interactionsManager = InteractionsManager(
		this,
		interaKTions
	)

	val gabrielaImageServerClient = GabrielaImageServerClient(
		config.loritta.gabrielaImageServer.url,
		HttpClient {
			// Increase the default timeout for image generation, because some video generations may take too long to be generated
			install(HttpTimeout) {
				this.socketTimeoutMillis = 60_000
				this.requestTimeoutMillis = 60_000
				this.connectTimeoutMillis = 60_000
			}
		}
	)
	val mojangApi = MinecraftMojangAPI()
	val correiosClient = CorreiosClient()
	val randomRoleplayPicturesClient = RandomRoleplayPicturesClient(config.loritta.randomRoleplayPictures.url)
	val falatronModelsManager = FalatronModelsManager().also {
		it.startUpdater()
	}
	val falatron = Falatron(config.loritta.falatron.url, config.loritta.falatron.key)
	val soundboard = Soundboard()
	// TODO: This is very hacky, maybe this could be improved somehow?
	lateinit var commandMentions: CommandMentions
	val unicodeEmojiManager = UnicodeEmojiManager()
	val emojiImageCache = EmojiImageCache()
	val graphicsFonts = GraphicsFonts()
	val googleTranslateClient = HackyGoogleTranslateClient()
	val googleVisionOCRClient = GoogleVisionOCRClient(config.loritta.googleVision.key)

	// ===[ LORITTA ]===
	lateinit var lorittaShards: LorittaShards
	val webhookExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), ThreadFactoryBuilder().setNameFormat("Webhook Sender %d").build())
	val webhookOkHttpClient = OkHttpClient()

	val legacyCommandManager = CommandManager(this) // Nosso command manager
	var messageInteractionCache = Caffeine.newBuilder().maximumSize(1000L).expireAfterAccess(3L, TimeUnit.MINUTES).build<Long, MessageInteractionFunctions>().asMap()
	val interactivityManager = InteractivityManager()
	var ignoreIds = mutableSetOf<Long>() // IDs para serem ignorados nesta sessão
	val apiCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<String, Long>().asMap()

	var discordListener = DiscordListener(this) // Vamos usar a mesma instância para todas as shards
	var eventLogListener = EventLogListener(this) // Vamos usar a mesma instância para todas as shards
	var messageListener = MessageListener(this)
	var voiceChannelListener = VoiceChannelListener(this)
	val gatewayRelayerListener = GatewayEventRelayerListener(this)
	val addReactionFurryAminoPtListener = AddReactionFurryAminoPtListener(this)
	val boostGuildListener = BoostGuildListener(this)
	val interactionsListener = InteractionsListener(this)
	val christmasListener = ReactionListener(this)

	var builder: DefaultShardManagerBuilder

	lateinit var raffleThread: RaffleThread
	lateinit var bomDiaECia: BomDiaECia

	var newWebsite: LorittaWebsite? = null
	var newWebsiteThread: Thread? = null

	var twitch = TwitchAPI(config.loritta.twitch.clientId, config.loritta.twitch.clientSecret)
	val connectionManager = ConnectionManager(this)
	var patchData = PatchData()
	var sponsors: List<Sponsor> = listOf()
	val cachedRetrievedArtists = CacheBuilder.newBuilder().expireAfterWrite(7, TimeUnit.DAYS)
		.build<Long, Optional<CachedUserInfo>>()
	var bucketedController: BucketedController? = null
	val rateLimitChecker = RateLimitChecker(this)

	val perfectPaymentsClient = PerfectPaymentsClient(config.loritta.perfectPayments.url)

	val commandMap = DiscordCommandMap(this)
	val assets = JVMLorittaAssets(this)
	var legacyLocales = mapOf<String, LegacyBaseLocale>()
	val http = HttpClient(Apache) {
		this.expectSuccess = false

		engine {
			this.socketTimeout = 25_000
			this.connectTimeout = 25_000
			this.connectionRequestTimeout = 25_000

			customizeClient {
				// Maximum number of socket connections.
				this.setMaxConnTotal(100)

				// Maximum number of requests for a specific endpoint route.
				this.setMaxConnPerRoute(100)
			}
		}
	}
	val httpWithoutTimeout = HttpClient(Apache) {
		this.expectSuccess = false

		engine {
			this.socketTimeout = 60_000
			this.connectTimeout = 60_000
			this.connectionRequestTimeout = 60_000

			customizeClient {
				// Maximum number of socket connections.
				this.setMaxConnTotal(100)

				// Maximum number of requests for a specific endpoint route.
				this.setMaxConnPerRoute(100)
			}
		}
	}
	val dreamStorageService = DreamStorageServiceClient(
		config.loritta.dreamStorageService.url,
		config.loritta.dreamStorageService.token,
		httpWithoutTimeout
	)

	val random = SecureRandom()
	val gifsicle = Gifsicle(config.loritta.binaries.gifsicle)

	val fanArtArtists = LorittaBot::class.getPathFromResources("/fan_arts_artists/")!!
		.let { Files.list(it).toList() }
		.map {
			loadFanArtArtist(it.inputStream())
		}

	val fanArts: List<FanArt>
		get() = fanArtArtists.flatMap { it.fanArts }
	val profileDesignManager = ProfileDesignManager(this)

	val isMainInstance = clusterId == 1

	val cachedServerConfigs = Caffeine.newBuilder()
		.maximumSize(100)
		.expireAfterWrite(300, TimeUnit.SECONDS)
		.build<Long, ServerConfig>()

	// Used for message execution
	val coroutineMessageExecutor = createThreadPool("Message Executor Thread %d")
	val coroutineMessageDispatcher = coroutineMessageExecutor.asCoroutineDispatcher() // Coroutine Dispatcher

	val coroutineExecutor = createThreadPool("Coroutine Executor Thread %d")
	val coroutineDispatcher = coroutineExecutor.asCoroutineDispatcher() // Coroutine Dispatcher
	fun createThreadPool(name: String) = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())

	val pendingMessages = ConcurrentLinkedQueue<Job>()
	val commandCooldownManager = CommandCooldownManager(this)
	val giveawayManager = GiveawayManager(this)
	val welcomeModule = WelcomeModule(this)
	val ecbManager = ECBManager()
	val activityUpdater = ActivityUpdater(this)

	private val debugWebServer = DebugWebServer()

	val preLoginStates = mutableMapOf<Int, MutableStateFlow<PreStartGatewayEventReplayListener.ProcessorState>>()
	var isActive = true

	init {
		FOLDER = config.loritta.folders.root
		ASSETS = config.loritta.folders.assets
		TEMP = config.loritta.folders.temp
		FRONTEND = config.loritta.folders.website

		val dispatcher = Dispatcher()
		dispatcher.maxRequestsPerHost = config.loritta.discord.maxRequestsPerHost

		val okHttpBuilder = OkHttpClient.Builder()
			.dispatcher(dispatcher)
			.connectTimeout(config.loritta.discord.okHttp.connectTimeout, TimeUnit.SECONDS) // O padrão de timeouts é 10 segundos, mas vamos aumentar para evitar problemas.
			.readTimeout(config.loritta.discord.okHttp.readTimeout, TimeUnit.SECONDS)
			.writeTimeout(config.loritta.discord.okHttp.writeTimeout, TimeUnit.SECONDS)
			.protocols(listOf(Protocol.HTTP_1_1)) // https://i.imgur.com/FcQljAP.png

		for (shardId in lorittaCluster.minShard..lorittaCluster.maxShard) {
			val initialSession = initialSessions[shardId]
			val state = MutableStateFlow(
				if (initialSession != null) {
					PreStartGatewayEventReplayListener.ProcessorState.WAITING_FOR_WEBSOCKET_CONNECTION
				} else {
					PreStartGatewayEventReplayListener.ProcessorState.FINISHED
				}
			)
			logger.info { "Shard $shardId status: ${state.value}" }

			preLoginStates[shardId] = state
		}

		builder = DefaultShardManagerBuilder.create(
			config.loritta.discord.token,
			GatewayIntent.MESSAGE_CONTENT,
			GatewayIntent.GUILD_MEMBERS,
			GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
			GatewayIntent.GUILD_BANS,
			GatewayIntent.GUILD_INVITES,
			GatewayIntent.GUILD_MESSAGES,
			GatewayIntent.GUILD_MESSAGE_REACTIONS,
			GatewayIntent.GUILD_VOICE_STATES,
			GatewayIntent.DIRECT_MESSAGES,
			GatewayIntent.DIRECT_MESSAGE_REACTIONS
		)
			// By default all flags are enabled, so we disable all flags and then...
			.disableCache(CacheFlag.values().toList())
			// ...we enable all the flags again
			.enableCache(
				CacheFlag.EMOJI,
				CacheFlag.STICKER,
				CacheFlag.MEMBER_OVERRIDES,
				CacheFlag.VOICE_STATE
			)
			.setChunkingFilter(ChunkingFilter.NONE) // No chunking policy because trying to load all members is hard
			.setMemberCachePolicy(MemberCachePolicy.ALL) // Cache all members!!
			.apply {
				logger.info { "Using shard controller (for bots with \"sharding for very large bots\" to manage shards!" }
				bucketedController = BucketedController(this@LorittaBot)
				this.setSessionController(bucketedController)
			}
			.setShardsTotal(config.loritta.discord.maxShards)
			.setShards(lorittaCluster.minShard.toInt(), lorittaCluster.maxShard.toInt())
			.setStatus(OnlineStatus.ONLINE)
			.setBulkDeleteSplittingEnabled(false)
			// Required for PreProcessedRawGatewayEvent and RawGatewayEvent
			.setHttpClientBuilder(okHttpBuilder)
			.setRawEventsEnabled(true)
			// We want to override JDA's shutdown hook to store the cache on disk when shutting down
			.setEnableShutdownHook(false)
			.addEventListeners(
				discordListener,
				eventLogListener,
				messageListener,
				voiceChannelListener,
				gatewayRelayerListener,
				addReactionFurryAminoPtListener,
				boostGuildListener,
				interactionsListener,
				christmasListener
			)
			.addEventListenerProvider {
				PreStartGatewayEventReplayListener(
					this,
					initialSessions[it],
					cacheFolder,
					preLoginStates[it]!!,
				)
			}
	}

	val lorittaCluster: LorittaConfig.LorittaClustersConfig.LorittaClusterConfig
		get() {
			return config.loritta.clusters.instances.first { it.id == clusterId }
		}

	val lorittaInternalApiKey: LorittaConfig.WebsiteConfig.AuthenticationKey
		get() {
			return config.loritta.website.apiKeys.first { it.description == "Loritta Internal Key" }
		}

	val activeEvents = ConcurrentLinkedQueue<Job>()

	val prometheusPushClient = PrometheusPushClient("loritta-morenitta", config.loritta.prometheusPush.url)
	val voiceConnectionsManager = LorittaVoiceConnectionManager(this)

	val scope = CoroutineScope(Dispatchers.Default)

	val analyticHandlers = mutableListOf<EventAnalyticsTask.AnalyticHandler>()
	val cinnamonTasks = CinnamonTasks(this)
	val tasksScope = CoroutineScope(Dispatchers.Default)

	private val starboardModule = StarboardModule(this)
	private val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)
	private val bomDiaECiaModule = BomDiaECiaModule(this)
	private val debugGatewayModule = DebugGatewayModule(this)
	private val owoGatewayModule = OwOGatewayModule(this)
	private val afkModule = AFKModule(this)

	// This is executed sequentially!
	val modules = listOf(
		afkModule,
		addFirstToNewChannelsModule,
		starboardModule,
		owoGatewayModule,
		debugGatewayModule
	)

	// Inicia a Loritta
	@OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class, ExperimentalSerializationApi::class,
		ExperimentalSerializationApi::class
	)
	fun start() {
		logger.info { "Starting Debug Web Server..." }
		debugWebServer.start()

		logger.info { "Registering Prometheus Collectors..." }
		Prometheus.register()

		logger.info { "Success! Creating folders..." }
		File(FOLDER).mkdirs()
		File(ASSETS).mkdirs()
		File(TEMP).mkdirs()
		File(FRONTEND).mkdirs()

		logger.info { "Success! Loading locales..." }

		loadLegacyLocales()

		logger.info { "Success! Loading emotes..." }

		Emotes.emoteManager = DiscordEmoteManager(this).also { it.loadEmotes() }

		logger.info { "Success! Connecting to the database..." }

		initPostgreSql()

		try {
			logger.info("Sucesso! Iniciando Loritta (Website)...")
			startWebServer()
		} catch(e: Exception) {
			logger.info(e) { "Failed to start Loritta's webserver" }
		}

		logger.info { "Registering interactions features..." }
		interactionsManager.register()

		logger.info { "Starting Pudding tasks..." }
		pudding.startPuddingTasks()
		GlobalScope.launch(block = NitroBoostUtils.createBoostTask(this, config.loritta.donatorsOstentation))

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		logger.info { "Starting Loritta (Discord Bot)..." }

		val shardManager = builder.build()
		lorittaShards = LorittaShards(
			this,
			shardManager
		)

		logger.info { "Starting Cinnamon tasks..." }
		cinnamonTasks.start()
		startTasks()

		logger.info { "Sucesso! Iniciando threads da Loritta..." }

		logger.info { "Iniciando Tasks..." }
		val tasks = LorittaTasks(this)
		tasks.startTasks()

		logger.info { "Iniciando threads de reminders..." }
		RemindersThread(this).start()

		logger.info { "Iniciando bom dia & cia..." }
		bomDiaECia = BomDiaECia(this)

		if (this.isMainInstance) {
			logger.info { "Loading raffle..." }
			val raffleData = runBlocking {
				newSuspendedTransaction {
					MiscellaneousData.select { MiscellaneousData.id eq RaffleThread.DATA_KEY }
						.limit(1)
						.firstOrNull()
						?.get(MiscellaneousData.data)
				}
			}

			if (raffleData != null) {
				logger.info { "Parsing the JSON object..." }
				val json = JsonParser.parseString(raffleData).obj

				logger.info { "Loaded raffle data! ${RaffleThread.started}; ${json["lastWinnerId"].nullString}; ${json["lastWinnerPrize"].nullInt}" }
				RaffleThread.started = json["started"].long
				RaffleThread.lastWinnerId = json["lastWinnerId"].nullLong
				RaffleThread.lastWinnerPrize = json["lastWinnerPrize"].nullInt ?: 0
				val userIdArray = json["userIds"].nullArray

				if (userIdArray != null) {
					logger.info { "Loading ${userIdArray.size()} raffle user entries..." }
					val firstUserIdEntry = userIdArray.firstOrNull()
					if (firstUserIdEntry != null) {
						logger.info { "Loading directly from the JSON array..." }
						RaffleThread.userIds.addAll(userIdArray.map { it.long })
					}
				}
			}

			RaffleThread.isReady = true
			raffleThread = RaffleThread(this)
			raffleThread.start()
		}

		// Ou seja, agora a Loritta está funcionando, Yay!
		Runtime.getRuntime().addShutdownHook(
			thread(false) {
				// Mark this as shutdown to avoid dispatching jobs
				isActive = false

				// Remove all event listeners to make Loritta not process new events while restarting
				shardManager.shards.forEach { shard ->
					shard.removeEventListener(*shard.registeredListeners.toTypedArray())
				}

				logger.info { "Disconnecting from all voice channels..." }
				// TODO: You need to wait a bit until JDA fully shuts down the voice connection
				runBlocking {
					for ((guildId, voiceConnection) in voiceConnectionsManager.voiceConnections.toMap()) {
						logger.info { "Shutting down voice connection @ $guildId" }
						voiceConnectionsManager.shutdownVoiceConnection(guildId, voiceConnection)
					}
				}
				logger.info { "Disconnected from all voice channels!" }

				// This is used to validate if our cache was successfully written or not
				val connectionVersion = UUID.randomUUID()

				File(cacheFolder, "version").writeText(connectionVersion.toString())

				shardManager.shards.forEach { jda ->
					// Indicate on our presence that we are restarting
					jda.presence.setPresence(
						OnlineStatus.IDLE,
						Activity.playing(
							createActivityText(
								"\uD83D\uDE34 Loritta is restarting...",
								jda.shardInfo.shardId
							)
						)
					)
				}

				measureTime {
					shardManager.shards.forEach { shard ->
						measureTime {
							val jdaImpl = shard as JDAImpl
							val sessionId = jdaImpl.client.sessionId
							val resumeUrl = jdaImpl.client.resumeUrl
							val newLineUtf8 = "\n".toByteArray(Charsets.UTF_8)

							// Only get connected shards, invalidate everything else
							if (shard.status != JDA.Status.CONNECTED || sessionId == null || resumeUrl == null) {
								logger.info { "Fully shutting down shard ${shard.shardInfo.shardId}..." }
								// Not connected, shut down and invalidate our cached data
								shard.shutdownNow(1000) // We don't care about persisting our gateway session
								File(cacheFolder, shard.shardInfo.shardId.toString()).deleteRecursively()
							} else {
								logger.info { "Shutting down shard ${shard.shardInfo.shardId} to be resumed later..." }

								// Connected, store to the cache
								// Using close code 1012 does not invalidate your gateway session!
								shard.shutdownNow(1012)

								val shardCacheFolder = File(cacheFolder, shard.shardInfo.shardId.toString())

								// Delete the current cached data for this shard
								shardCacheFolder.deleteRecursively()

								// Create the shard cache folder
								shardCacheFolder.mkdirs()

								val guildsCacheFile = File(shardCacheFolder, "guilds.json")
								val sessionCacheFile = File(shardCacheFolder, "session.json")
								val versionFile = File(shardCacheFolder, "version")

								val guildIdsForReadyEvent =
									jdaImpl.guildsView.map { it.idLong } + jdaImpl.unavailableGuilds.map { it.toLong() }

								val guildCount = jdaImpl.guildsView.size()

								logger.info { "Trying to persist ${guildCount} guilds for shard ${jdaImpl.shardInfo.shardId}..." }

								val byteArrayChannel = Channel<ByteArray>()

								val fileOutputStreamJob = GlobalScope.launch(Dispatchers.IO) {
									FileOutputStream(guildsCacheFile).use {
										for (byteArray in byteArrayChannel) {
											it.write(byteArray)
											it.write(newLineUtf8)
										}
									}
								}

								val jobs = jdaImpl.guildsView.map { guild ->
									GlobalScope.launch(Dispatchers.IO) {
										guild as GuildImpl

										// We want to minimize resizes of the backed stream, to make it go zooooom
										// So what I did was calculating the 75th percentile of the guild data, and used it as the size
										val baos = ByteArrayOutputStream(16_384)
										Json.encodeToStream(DeviousConverter.toJson(guild), baos)

										// Remove the guild from memory, which avoids the bot crashing due to Out Of Memory
										guild.invalidate()

										byteArrayChannel.send(baos.toByteArray())
									}
								}

								runBlocking { jobs.joinAll() }
								// Already sent all data to the channel, cancel the channel!
								byteArrayChannel.cancel()
								runBlocking {
									fileOutputStreamJob.join()
								}

								logger.info { "Writing session cache file for shard ${jdaImpl.shardInfo.shardId}..." }
								sessionCacheFile
									.writeText(
										Json.encodeToString(
											GatewaySessionData(
												sessionId,
												resumeUrl,
												jdaImpl.responseTotal,
												guildIdsForReadyEvent
											)
										)
									)

								// Only write after everything has been successfully written
								versionFile.writeText(connectionVersion.toString())
							}
						}.also { logger.info { "Took $it to process shard's ${shard.shardInfo.shardId} stuff!" } }
					}
				}.also { logger.info { "Took $it to persist all shards cache!!" } }
			}
		)

		logger.info { "Yay! Loritta is up and running :3" }
	}

	fun initPostgreSql() {
		logger.info("Iniciando PostgreSQL...")

		transaction {
			createOrUpdatePostgreSQLEnum(BackgroundStorageType.values())
			createOrUpdatePostgreSQLEnum(LoriTuberContentLength.values())
			createOrUpdatePostgreSQLEnum(LoriTuberContentType.values())
			createOrUpdatePostgreSQLEnum(LoriTuberContentGenre.values())

			// TODO: Fix pudding tables to check if they aren't going to *explode* when we set up it to register all tables
			SchemaUtils.createMissingTablesAndColumns(
				GatewayActivities,
				UserSettings,
				Backgrounds,
				BackgroundVariations,
				ConcurrentLoginBuckets,
				Christmas2022Players,
				Christmas2022Drops,
				CollectedChristmas2022Points,
				Christmas2022SonhosTransactionsLog
			)
		}

		// Hidden behind a env flag, because FOR SOME REASON Exposed thinks that it is a good idea to
		// "ALTER TABLE serverconfigs ALTER COLUMN prefix TYPE TEXT, ALTER COLUMN prefix SET DEFAULT '+'"
		// And that LOCKS the ServerConfig table, and sometimes that takes a LOOOONG time to complete, which locks up everything
		if (System.getenv("LORITTA_CREATE_TABLES") != null) {
			runBlocking {
				pudding.createMissingTablesAndColumns { true }

				pudding.transaction {
					SchemaUtils.createMissingTablesAndColumns(
						StoredMessages,
						Profiles,
						UserSettings,
						Reminders,
						Reputations,
						Dailies,
						Marriages,
						Mutes,
						Warns,
						GuildProfiles,
						Giveaways,
						ReactionOptions,
						ServerConfigs,
						DonationKeys,
						Payments,
						ShipEffects,
						BotVotes,
						StoredMessages,
						StarboardMessages,
						Sponsors,
						EconomyConfigs,
						ExecutedCommandsLog,
						BlacklistedGuilds,
						RolesByExperience,
						LevelAnnouncementConfigs,
						LevelConfigs,
						AuditLog,
						ExperienceRoleRates,
						BomDiaECiaWinners,
						TrackedTwitterAccounts,
						SonhosTransaction,
						TrackedYouTubeAccounts,
						TrackedTwitchAccounts,
						CachedYouTubeChannelIds,
						SonhosBundles,
						Backgrounds,
						BackgroundVariations,
						Sets,
						DailyShops,
						DailyShopItems,
						BackgroundPayments,
						CachedDiscordUsers,
						SentYouTubeVideoIds,
						SpicyStacktraces,
						BannedIps,
						DonationConfigs,
						StarboardConfigs,
						MiscellaneousConfigs,
						EventLogConfigs,
						AutoroleConfigs,
						InviteBlockerConfigs,
						ServerRolePermissions,
						WelcomerConfigs,
						CustomGuildCommands,
						MemberCounterChannelConfigs,
						ModerationConfigs,
						WarnActions,
						ModerationPunishmentMessagesConfig,
						BannedUsers,
						ProfileDesigns,
						ProfileDesignsPayments,
						ProfileDesignGroups,
						ProfileDesignGroupEntries,
						DailyProfileShopItems,
						CachedDiscordWebhooks,
						CustomBackgroundSettings
					)
				}
			}
		}

		TrinketsStuff.updateTrinkets(pudding)
	}

	fun startWebServer() {
		// Carregar os blog posts
		newWebsiteThread = thread(true, name = "Website Thread") {
			// Loads the appropriate bundle depending if we are overriding the JS file or not
			val spicyMorenittaJsBundle = if (this@LorittaBot.config.loritta.website.spicyMorenittaJsPath != null) {
				SpicyMorenittaDevelopmentBundle(this@LorittaBot)
			} else {
				SpicyMorenittaProductionBundle(
					SpicyMorenittaBundle.createSpicyMorenittaJsBundleContent(
						LorittaWebsite::class.getPathFromResources("/spicy_morenitta/js/spicy-morenitta.js")!!.readText()
					)
				)
			}

			val nWebsite = LorittaWebsite(this, lorittaCluster.websiteUrl, config.loritta.folders.website, spicyMorenittaJsBundle)
			newWebsite = nWebsite
			nWebsite.start()
		}
	}

	fun stopWebServer() {
		newWebsite?.stop()
		newWebsiteThread?.interrupt()
	}

	/**
	 * Gets an user's profile background image or, if the user has a custom background, loads the custom background.
	 *
	 * To avoid exceeding the available memory, profiles are loaded from the "cropped_profiles" folder,
	 * which has all the images in 800x600 format.
	 *
	 * @param background the user's background
	 * @return the background image
	 */
	suspend fun getUserProfileBackground(profile: Profile): BufferedImage {
		val backgroundUrl = getUserProfileBackgroundUrl(profile)
		val response = this.http.get(backgroundUrl) {
			userAgent(lorittaCluster.getUserAgent(this@LorittaBot))
		}

		val bytes = response.readBytes()

		return readImage(bytes.inputStream())
	}

	/**
	 * Gets an user's profile background URL
	 *
	 * This does *not* crop the profile background
	 *
	 * @param profile the user's profile
	 * @return the background image
	 */
	suspend fun getUserProfileBackgroundUrl(profile: Profile): String {
		val settingsId = this.newSuspendedTransaction { profile.settings.id.value }
		val activeProfileDesignInternalName = this.newSuspendedTransaction { profile.settings.activeProfileDesignInternalName }?.value
		val activeBackgroundInternalName = this.newSuspendedTransaction { profile.settings.activeBackgroundInternalName }?.value
		return getUserProfileBackgroundUrl(profile.userId, settingsId, activeProfileDesignInternalName ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID, activeBackgroundInternalName ?: Background.DEFAULT_BACKGROUND_ID)
	}

	/**
	 * Gets an user's profile background URL
	 *
	 * This does *not* crop the profile background
	 *
	 * @param profile the user's profile
	 * @return the background image
	 */
	suspend fun getUserProfileBackgroundUrl(profile: PuddingUserProfile): String {
		val profileSettings = profile.getProfileSettings()
		val activeProfileDesignInternalName = profileSettings.activeProfileDesign
		val activeBackgroundInternalName = profileSettings.activeBackground
		// TODO: Fix default profile design ID
		return getUserProfileBackgroundUrl(profile.id.value.toLong(), profileSettings.id, activeProfileDesignInternalName ?: ProfileDesign.DEFAULT_PROFILE_DESIGN_ID, activeBackgroundInternalName ?: Background.DEFAULT_BACKGROUND_ID)
	}

	/**
	 * Gets an user's profile background URL
	 *
	 * This does *not* crop the profile background
	 *
	 * @param profile the user's profile
	 * @return the background image
	 */
	suspend fun getUserProfileBackgroundUrl(
		userId: Long,
		settingsId: Long,
		activeProfileDesignInternalName: String,
		activeBackgroundInternalName: String
	): String {
		val defaultBlueBackground = this.pudding.backgrounds.getBackground(Background.DEFAULT_BACKGROUND_ID)!!
		var background = pudding.backgrounds.getBackground(activeBackgroundInternalName) ?: defaultBlueBackground

		if (background.id == Background.RANDOM_BACKGROUND_ID) {
			// If the user selected a random background, we are going to get all the user's backgrounds and choose a random background from the list
			val allBackgrounds = mutableListOf(defaultBlueBackground)

			allBackgrounds.addAll(
				this.newSuspendedTransaction {
					(BackgroundPayments innerJoin Backgrounds).select {
						BackgroundPayments.userId eq userId
					}.map {
						val data = Background.fromRow(it)
						PuddingBackground(
							pudding,
							data
						)
					}
				}
			)

			background = allBackgrounds.random()
		}

		if (background.id == Background.CUSTOM_BACKGROUND_ID) {
			// Custom background
			val donationValue = this.getActiveMoneyFromDonationsAsync(userId)
			val plan = UserPremiumPlans.getPlanFromValue(donationValue)

			if (plan.customBackground) {
				val dssNamespace = this.dreamStorageService.getCachedNamespaceOrRetrieve()
				val resultRow = this.newSuspendedTransaction {
					CustomBackgroundSettings.select { CustomBackgroundSettings.settings eq settingsId }
						.firstOrNull()
				}

				// If the path exists, then the background (probably!) exists
				if (resultRow != null) {
					val file = resultRow[net.perfectdreams.loritta.morenitta.tables.CustomBackgroundSettings.file]
					val extension = MediaTypeUtils.convertContentTypeToExtension(resultRow[net.perfectdreams.loritta.morenitta.tables.CustomBackgroundSettings.preferredMediaType])
					return "${this.config.loritta.dreamStorageService.url}/$dssNamespace/${StoragePaths.CustomBackground(userId, file).join()}.$extension"
				}
			}

			// If everything fails, change the background to the default blue background
			// This is required because the current background is "CUSTOM", so Loritta will try getting the default variation of the custom background...
			// but that doesn't exist!
			background = defaultBlueBackground
		}

		val dssNamespace = dreamStorageService.getCachedNamespaceOrRetrieve()
		val variation = background.getVariationForProfileDesign(activeProfileDesignInternalName)
		return when (variation.storageType) {
			BackgroundStorageType.DREAM_STORAGE_SERVICE -> getDreamStorageServiceBackgroundUrlWithCropParameters(this.config.loritta.dreamStorageService.url, dssNamespace, variation)
			BackgroundStorageType.ETHEREAL_GAMBI -> getEtherealGambiBackgroundUrl(variation)
		}
	}

	private fun getDreamStorageServiceBackgroundUrl(
		dreamStorageServiceUrl: String,
		namespace: String,
		background: BackgroundVariation
	): String {
		val extension = MediaTypeUtils.convertContentTypeToExtension(background.preferredMediaType)
		return "$dreamStorageServiceUrl/$namespace/${StoragePaths.Background(background.file).join()}.$extension"
	}

	private fun getDreamStorageServiceBackgroundUrlWithCropParameters(
		dreamStorageServiceUrl: String,
		namespace: String,
		variation: BackgroundVariation
	): String {
		var url = getDreamStorageServiceBackgroundUrl(dreamStorageServiceUrl, namespace, variation)
		val crop = variation.crop
		if (crop != null)
			url += "?crop_x=${crop.x}&crop_y=${crop.y}&crop_width=${crop.width}&crop_height=${crop.height}"
		return url
	}

	private fun getEtherealGambiBackgroundUrl(background: BackgroundVariation): String {
		val extension = MediaTypeUtils.convertContentTypeToExtension(background.preferredMediaType)
		return config.loritta.etherealGambiService.url.removeSuffix("/") + "/" + background.file + ".$extension"
	}

	/**
	 * Loads an specific fan art artist
	 */
	private fun loadFanArtArtist(inputStream: InputStream): FanArtArtist = Constants.HOCON_MAPPER.readValue(inputStream)

	fun getFanArtArtistByFanArt(fanArt: FanArt) = fanArtArtists.firstOrNull { fanArt in it.fanArts }

	/**
	 * Initializes the available locales and adds missing translation strings to non-default languages
	 *
	 * @see LegacyBaseLocale
	 */
	fun loadLegacyLocales() {
		val locales = mutableMapOf<String, LegacyBaseLocale>()

		val legacyLocalesFolder = LorittaBot::class.getPathFromResources("/locales/legacy/")!!

		// Carregar primeiro o locale padrão
		val defaultLocaleFile = LorittaBot::class.getPathFromResources("/locales/legacy/default.json")!!
		val localeAsText = defaultLocaleFile.readText(Charsets.UTF_8)
		val defaultLocale = LorittaBot.GSON.fromJson(localeAsText, LegacyBaseLocale::class.java) // Carregar locale do jeito velho
		val defaultJsonLocale = JsonParser.parseString(localeAsText).obj // Mas também parsear como JSON

		defaultJsonLocale.entrySet().forEach { (key, value) ->
			if (!value.isJsonArray) { // TODO: Listas!
				defaultLocale.strings[key] = value.string
			}
		}

		// E depois guardar o nosso default locale
		locales.put("default", defaultLocale)

		// Carregar todos os locales
		val localesFolder = legacyLocalesFolder
		val prettyGson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
		for (file in localesFolder.listDirectoryEntries()) {
			if (file.extension == "json" && file.nameWithoutExtension != "default") {
				// Carregar o BaseLocale baseado no locale atual
				val localeAsText = file.readText(Charsets.UTF_8)
				val locale = prettyGson.fromJson(localeAsText, LegacyBaseLocale::class.java)
				locale.strings = HashMap<String, String>(defaultLocale.strings) // Clonar strings do default locale
				locales.put(file.nameWithoutExtension, locale)
				// Yay!
			}
		}

		// E agora preencher valores nulos e salvar as traduções
		for ((id, locale) in locales) {
			if (id != "default") {
				val jsonObject = JsonParser.parseString(LorittaBot.GSON.toJson(locale))

				val localeFile = LorittaBot::class.getPathFromResources("/locales/legacy/$id.json")!!
				val asJson = JsonParser.parseString(localeFile.readText()).obj

				for ((id, obj) in asJson.entrySet()) {
					if (obj.isJsonPrimitive && obj.asJsonPrimitive.isString) {
						locale.strings.put(id, obj.string)
					}
				}

				// Usando Reflection TODO: Remover
				for (field in locale::class.java.declaredFields) {
					if (field.name == "strings" || Modifier.isStatic(field.modifiers)) {
						continue
					}
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
			}
		}

		this.legacyLocales = locales
	}

	/**
	 * Gets the BaseLocale from the ID, if the locale doesn't exist, the default locale ("default") will be retrieved
	 *
	 * @param localeId the ID of the locale
	 * @return         the locale on BaseLocale format or, if the locale doesn't exist, the default locale will be loaded
	 * @see            LegacyBaseLocale
	 */
	@Deprecated("Please use getLocaleById")
	fun getLegacyLocaleById(localeId: String): LegacyBaseLocale {
		return legacyLocales.getOrDefault(localeId, legacyLocales["default"]!!)
	}

	fun <T> transaction(statement: Transaction.() -> T) = runBlocking {
		pudding.transaction {
			statement.invoke(this)
		}
	}

	suspend fun <T> newSuspendedTransaction(repetitions: Int = 5, transactionIsolation: Int = Connection.TRANSACTION_REPEATABLE_READ, statement: Transaction.() -> T): T
			= pudding.transaction(repetitions, transactionIsolation, statement)

	suspend fun <T> suspendedTransactionAsync(statement: Transaction.() -> T) = GlobalScope.async(coroutineDispatcher) {
		newSuspendedTransaction(statement = statement)
	}


	/**
	 * Gets an user's profile background
	 *
	 * @param id the user's ID
	 * @return the background image
	 */
	suspend fun getUserProfileBackground(id: Long) = getUserProfileBackground(getOrCreateLorittaProfile(id))

	/**
	 * Loads the server configuration of a guild
	 *
	 * @param guildId the guild's ID
	 * @return        the server configuration
	 */
	fun getOrCreateServerConfig(guildId: Long, loadFromCache: Boolean = false): ServerConfig {
		if (loadFromCache)
			cachedServerConfigs.getIfPresent(guildId)?.let { return it }

		return runBlocking {
			pudding.transaction {
				_getOrCreateServerConfig(guildId)
			}
		}
	}

	/**
	 * Loads the server configuration of a guild in a coroutine
	 *
	 * @param guildId the guild's ID
	 * @return        the server configuration
	 */
	suspend fun getOrCreateServerConfigAsync(guildId: Long, loadFromCache: Boolean = false): ServerConfig {
		if (loadFromCache)
			cachedServerConfigs.getIfPresent(guildId)?.let { return it }

		return pudding.transaction { _getOrCreateServerConfig(guildId) }
	}

	/**
	 * Loads the server configuration of a guild, deferred
	 *
	 * @param guildId the guild's ID
	 * @return        the server configuration
	 */
	suspend fun getOrCreateServerConfigDeferred(guildId: Long, loadFromCache: Boolean = false): Deferred<ServerConfig> {
		if (loadFromCache)
			cachedServerConfigs.getIfPresent(guildId)?.let { return GlobalScope.async(coroutineDispatcher) { it } }

		return GlobalScope.async { pudding.transaction { _getOrCreateServerConfig(guildId) } }
	}

	private fun _getOrCreateServerConfig(guildId: Long): ServerConfig {
		val result = ServerConfig.findById(guildId) ?: ServerConfig.new(guildId) {}
		cachedServerConfigs.put(guildId, result)
		return result
	}

	fun getLorittaProfile(userId: String): Profile? {
		return getLorittaProfile(userId.toLong())
	}

	/**
	 * Loads the profile of an user
	 *
	 * @param userId the user's ID
	 * @return       the user profile
	 */
	fun getLorittaProfile(userId: Long) = runBlocking { pudding.transaction { _getLorittaProfile(userId) } }

	/**
	 * Loads the profile of an user in a coroutine
	 *
	 * @param userId the user's ID
	 * @return       the user profile
	 */
	suspend fun getLorittaProfileAsync(userId: Long) = pudding.transaction { _getLorittaProfile(userId) }

	/**
	 * Loads the profile of an user deferred
	 *
	 * @param userId the user's ID
	 * @return       the user profile
	 */
	suspend fun getLorittaProfileDeferred(userId: Long) = GlobalScope.async { pudding.transaction { _getLorittaProfile(userId) } }

	fun _getLorittaProfile(userId: Long) = Profile.findById(userId)

	fun getOrCreateLorittaProfile(userId: String): Profile {
		return getOrCreateLorittaProfile(userId.toLong())
	}

	fun getOrCreateLorittaProfile(userId: Long): Profile {
		val sqlProfile = transaction { Profile.findById(userId) }
		if (sqlProfile != null)
			return sqlProfile

		val profileSettings = transaction {
			ProfileSettings.new {
				gender = net.perfectdreams.loritta.common.utils.Gender.UNKNOWN
			}
		}

		return transaction {
			Profile.new(userId) {
				xp = 0
				lastMessageSentAt = 0L
				lastMessageSentHash = 0
				money = 0
				isAfk = false
				settings = profileSettings
			}
		}
	}

	fun getActiveMoneyFromDonations(userId: Long): Double {
		return transaction { _getActiveMoneyFromDonations(userId) }
	}

	suspend fun getActiveMoneyFromDonationsAsync(userId: Long): Double {
		return newSuspendedTransaction { _getActiveMoneyFromDonations(userId) }
	}

	fun _getActiveMoneyFromDonations(userId: Long): Double {
		return Payment.find {
			(Payments.expiresAt greaterEq System.currentTimeMillis()) and
					(Payments.reason eq PaymentReason.DONATION) and
					(Payments.userId eq userId)
		}.sumByDouble {
			// This is a weird workaround that fixes users complaining that 19.99 + 19.99 != 40 (it equals to 39.38()
			ceil(it.money.toDouble())
		}
	}

	fun launchMessageJob(event: Event, block: suspend CoroutineScope.() -> Unit) {
		val coroutineName = when (event) {
			is MessageReceivedEvent -> "Message ${event.message} by user ${event.author} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is SlashCommandInteractionEvent -> "Slash Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			else -> throw IllegalArgumentException("You can't dispatch a $event in a launchMessageJob!")
		}

		val start = System.currentTimeMillis()
		val job = GlobalScope.launch(
			coroutineMessageDispatcher + CoroutineName(coroutineName),
			block = block
		)
		// Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
		// added to the list, causing leaks.
		// invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
		pendingMessages.add(job)
		job.invokeOnCompletion {
			pendingMessages.remove(job)

			val diff = System.currentTimeMillis() - start
			if (diff >= 60_000) {
				logger.warn { "Message Coroutine $job took too long to process! ${diff}ms" }
			}
		}
	}

	fun isOwner(userId: String) = isOwner(Snowflake(userId))
	fun isOwner(userId: Long) = isOwner(Snowflake(userId))
	fun isOwner(userId: Snowflake) = userId in config.loritta.ownerIds

	suspend fun getCachedUserInfo(userId: Snowflake) = getCachedUserInfo(UserId(userId.value))

	suspend fun getCachedUserInfo(userId: UserId): net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo? {
		// First, try getting the cached user info from the database
		val cachedUserInfoFromDatabase = pudding.users.getCachedUserInfoById(userId)
		if (cachedUserInfoFromDatabase != null)
			return cachedUserInfoFromDatabase

		// If not present, get it from Discord!
		val restUser = try {
			rest.user.getUser(Snowflake(userId.value))
		} catch (e: KtorRequestException) {
			null
		}

		if (restUser != null) {
			// If the REST user really exists, then let's update it in our database and then return the cached user info
			pudding.users.insertOrUpdateCachedUserInfo(
				UserId(restUser.id.value),
				restUser.username,
				restUser.discriminator,
				restUser.avatar
			)

			return net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo(
				UserId(restUser.id.value),
				restUser.username,
				restUser.discriminator,
				restUser.avatar
			)
		}

		return null
	}

	suspend fun insertOrUpdateCachedUserInfo(user: User) {
		pudding.users.insertOrUpdateCachedUserInfo(
			UserId(user.id.value),
			user.username,
			user.discriminator,
			user.data.avatar
		)
	}

	suspend inline fun <reified T> encodeDataForComponentOnDatabase(data: T, ttl: Duration = 15.minutes): ComponentOnDatabaseStoreResult<T> {
		// Can't fit on a button... Let's store it on the database!
		val now = Clock.System.now()

		val interactionDataId = pudding.interactionsData.insertInteractionData(
			Json.encodeToJsonElement<T>(
				data
			).jsonObject,
			now,
			now + ttl
		)

		val storedGenericInteractionData = StoredGenericInteractionData(ComponentDataUtils.KTX_SERIALIZATION_SIMILAR_PROTOBUF_STRUCTURE_ISSUES_WORKAROUND_DUMMY, interactionDataId)

		return ComponentOnDatabaseStoreResult(
			interactionDataId,
			storedGenericInteractionData,
			ComponentDataUtils.encode(storedGenericInteractionData)
		)
	}

	suspend inline fun <reified T> decodeDataFromComponentOnDatabase(data: String): ComponentOnDatabaseQueryResult<T> {
		val genericInteractionData = ComponentDataUtils.decode<StoredGenericInteractionData>(data)

		val dataFromDatabase = pudding.interactionsData.getInteractionData(genericInteractionData.interactionDataId)
			?.jsonObject ?: return ComponentOnDatabaseQueryResult(genericInteractionData, null)

		return ComponentOnDatabaseQueryResult(genericInteractionData, Json.decodeFromJsonElement<T>(dataFromDatabase))
	}

	/**
	 * Encodes the [data] to fit on a button. If it doesn't fit in a button, a [StoredGenericInteractionData] will be encoded instead and the data will be stored on the database.
	 */
	suspend inline fun <reified T> encodeDataForComponentOrStoreInDatabase(data: T, ttl: Duration = 15.minutes): String {
		val encoded = ComponentDataUtils.encode(data)

		// Let's suppose that all components always have 5 characters at the start
		// (Technically it is true: Discord InteraKTions uses ":" as the separator, and we only use 4 chars for ComponentExecutorIds)
		val padStart = 5 // "0000:"

		if (100 - padStart >= encoded.length) {
			// Can fit on a button! So let's just return what we currently have
			return encoded
		} else {
			// Can't fit on a button... Let's store it on the database!
			return encodeDataForComponentOnDatabase(data, ttl).serializedData
		}
	}

	/**
	 * Decodes the [data] based on the source data:
	 * * If [data] is a [StoredGenericInteractionData], the data will be retrieved from the database and deserialized using [T]
	 * * If else, the data will be deserialized using [T]
	 *
	 * This should be used in conjuction with [encodeDataForComponentOrStoreInDatabase]
	 */
	suspend inline fun <reified T> decodeDataFromComponentOrFromDatabase(data: String): T? {
		return try {
			val result = decodeDataFromComponentOnDatabase<T>(data)
			result.data
		} catch (e: SerializationException) {
			// If the deserialization failed, then let's try deserializing as T
			ComponentDataUtils.decode<T>(data)
		}
	}

	data class ComponentOnDatabaseStoreResult<T>(
		val interactionDataId: Long,
		val data: StoredGenericInteractionData,
		val serializedData: String
	)

	data class ComponentOnDatabaseQueryResult<T>(
		val genericInteractionData: StoredGenericInteractionData,
		val data: T?
	)


	private fun launchEventJob(
		coroutineName: String,
		durations: Map<KClass<*>, Duration>,
		block: suspend CoroutineScope.() -> Unit
	) {
		val start = System.currentTimeMillis()

		val job = scope.launch(
			CoroutineName(coroutineName),
			block = block
		)

		activeEvents.add(job)
		DiscordGatewayEventsProcessorMetrics.activeEvents.set(activeEvents.size.toDouble())

		// Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
		// added to the list, causing leaks.
		// invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
		job.invokeOnCompletion {
			activeEvents.remove(job)
			DiscordGatewayEventsProcessorMetrics.activeEvents.set(activeEvents.size.toDouble())

			val diff = System.currentTimeMillis() - start
			if (diff >= 60_000) {
				logger.warn { "Coroutine $job ($coroutineName) took too long to process! ${diff}ms - Module Durations: $durations" }
			}
		}
	}

	@OptIn(ExperimentalTime::class)
	fun launchEventProcessorJob(context: GatewayEventContext) {
		if (context.event != null) {
			val coroutineName = "Event ${context.event::class.simpleName}"
			launchEventJob(coroutineName, context.durations) {
				try {
					for (module in modules) {
						val (result, duration) = measureTimedValue { module.processEvent(context) }
						context.durations[module::class] = duration
						DiscordGatewayEventsProcessorMetrics.executedModuleLatency
							.labels(module::class.simpleName!!, context.event::class.simpleName!!)
							.observe(duration.toDouble(DurationUnit.SECONDS))

						when (result) {
							ModuleResult.Cancel -> {
								// Module asked us to stop processing the events
								return@launchEventJob
							}
							ModuleResult.Continue -> {
								// Module asked us to continue processing the events
							}
						}
					}
				} catch (e: Throwable) {
					logger.warn(e) { "Something went wrong while trying to process $coroutineName! We are going to ignore..." }
				}
			}
		} else
			logger.warn { "Unknown Discord event received! We are going to ignore the event... kthxbye!" }
	}

	/**
	 * Gets the current registered application commands count
	 */
	fun getCommandCount() = interactionsManager.interaKTions.manager.applicationCommandsExecutors.size

	/**
	 * Sends the [builder] message to the [userId] via the user's direct message channel.
	 *
	 * The ID of the direct message channel is cached.
	 */
	suspend fun sendMessageToUserViaDirectMessage(userId: Snowflake, builder: UserMessageCreateBuilder.() -> (Unit)) = sendMessageToUserViaDirectMessage(
		UserId(userId),
		builder
	)

	/**
	 * Sends the [builder] message to the [userId] via the user's direct message channel.
	 *
	 * The ID of the direct message channel is cached.
	 */
	suspend fun sendMessageToUserViaDirectMessage(userId: UserId, builder: UserMessageCreateBuilder.() -> (Unit)) = UserUtils.sendMessageToUserViaDirectMessage(
		pudding,
		rest,
		userId,
		builder
	)

	/**
	 * Adds an analytic handler, used for debugging logs on the [EventAnalyticsTask]
	 */
	fun addAnalyticHandler(handler: EventAnalyticsTask.AnalyticHandler) = analyticHandlers.add(handler)

	/**
	 * Schedules [action] to be executed on [tasksScope] every [period] with a [initialDelay]
	 */
	private fun scheduleCoroutineAtFixedRate(
		period: Duration,
		initialDelay: Duration = Duration.ZERO,
		action: RunnableCoroutine
	) {
		logger.info { "Scheduling ${action::class.simpleName} to be ran every $period with a $initialDelay initial delay" }
		scheduleCoroutineAtFixedRate(tasksScope, period, initialDelay, action)
	}

	/**
	 * Schedules [action] to be executed on [tasksScope] every [period] with a [initialDelay] if this [isMainReplica]
	 */
	private fun scheduleCoroutineAtFixedRateIfMainReplica(
		period: Duration,
		initialDelay: Duration = Duration.ZERO,
		action: RunnableCoroutine
	) {
		if (isMainInstance)
			scheduleCoroutineAtFixedRate(period, initialDelay, action)
	}

	private fun scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(time: LocalTime, action: RunnableCoroutine) {
		val now = Instant.now()
		val today = LocalDate.now(ZoneOffset.UTC)
		val todayAtTime = LocalDateTime.of(today, time)
		val gonnaBeScheduledAtTime =  if (now > todayAtTime.toInstant(ZoneOffset.UTC)) {
			// If today at time is larger than today, then it means that we need to schedule it for tomorrow
			todayAtTime.plusDays(1)
		} else todayAtTime

		val diff = gonnaBeScheduledAtTime.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

		scheduleCoroutineAtFixedRateIfMainReplica(
			1.days,
			diff.milliseconds,
			action
		)
	}

	private fun startTasks() {
		scheduleCoroutineAtFixedRate(1.minutes, action = activityUpdater)
		scheduleCoroutineAtFixedRateIfMainReplica(15.seconds, action = CorreiosPackageInfoUpdater(this@LorittaBot))
		scheduleCoroutineAtFixedRateIfMainReplica(1.seconds, action = PendingImportantNotificationsProcessor(this@LorittaBot))
		scheduleCoroutineAtFixedRateIfMainReplica(1.minutes, action = LorittaStatsCollector(this@LorittaBot))
		// Christmas stuff
		if (LorittaChristmas2022Event.isEventActive() && config.loritta.environment == EnvironmentType.PRODUCTION) {
			scheduleCoroutineAtFixedRate(1.minutes) {
				try {
					if (!LorittaChristmas2022Event.isEventActive())
						return@scheduleCoroutineAtFixedRate

					val guild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

					if (guild != null) {
						val role = guild.getRoleById(1055877016739663872L) ?: return@scheduleCoroutineAtFixedRate

						val countColumn = CollectedChristmas2022Points.points.count()

						val topUserIds = newSuspendedTransaction {
							CollectedChristmas2022Points.slice(CollectedChristmas2022Points.user, countColumn)
								.select { CollectedChristmas2022Points.valid eq true }
								.groupBy(CollectedChristmas2022Points.user)
								.orderBy(countColumn to SortOrder.DESC)
								.limit(5, 0)
								.map { it[CollectedChristmas2022Points.user].value }
						}

						val currentMembersWithRole = guild.getMembersWithRoles(role)

						for (member in currentMembersWithRole) {
							if (member.idLong !in topUserIds) {
								// Bye
								member.guild.removeRoleFromMember(UserSnowflake.fromId(member.idLong), role).await()
							}
						}

						// Give the role for users that don't have the role yet
						for (userId in topUserIds) {
							val member = guild.getMemberById(userId) ?: continue

							if (!member.roles.contains(role)) {
								guild.addRoleToMember(UserSnowflake.fromId(userId), role).await()
							}
						}
					}
				} catch (e: Exception) {
					logger.warn(e) { "Something went wrong while trying to update the top christmas roles!" }
				}
			}
		}

		val dailyTaxWarner = DailyTaxWarner(this)
		val dailyTaxCollector = DailyTaxCollector(this)

		// 12 hours before
		scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(
			LocalTime.of(12, 0),
			dailyTaxWarner
		)

		// 4 hours before
		scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(
			LocalTime.of(20, 0),
			dailyTaxWarner
		)

		// 1 hour before
		scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(
			LocalTime.of(23, 0),
			dailyTaxWarner
		)

		// at midnight + notify about the user about taxes
		scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(
			LocalTime.MIDNIGHT,
			dailyTaxCollector
		)
	}

	/**
	 * Gets an user's profile background image or, if the user has a custom background, loads the custom background.
	 *
	 * To avoid exceeding the available memory, profiles are loaded from the "cropped_profiles" folder,
	 * which has all the images in 800x600 format.
	 *
	 * @param background the user's background
	 * @return the background image
	 */
	suspend fun getUserProfileBackground(profile: PuddingUserProfile): BufferedImage {
		val backgroundUrl = getUserProfileBackgroundUrl(profile)
		val response = http.get(backgroundUrl) {
			userAgent(lorittaCluster.getUserAgent(this@LorittaBot))
		}

		val bytes = response.readBytes()

		return net.perfectdreams.loritta.cinnamon.discord.utils.images.readImage(bytes.inputStream())
	}

	fun createActivityText(activityText: String, shardId: Int) = "$activityText | Cluster ${lorittaCluster.id} [$shardId]"
}