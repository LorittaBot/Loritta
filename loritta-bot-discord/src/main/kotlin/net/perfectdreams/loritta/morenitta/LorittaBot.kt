package net.perfectdreams.loritta.morenitta

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.luben.zstd.Zstd
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.managers.AudioManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestConfig
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.dv8tion.jda.internal.JDAImpl
import net.dv8tion.jda.internal.entities.GuildImpl
import net.perfectdreams.discordinteraktions.common.DiscordInteraKTions
import net.perfectdreams.discordinteraktions.common.commands.MessageCommandDeclaration
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.common.commands.UserCommandDeclaration
import net.perfectdreams.dreamstorageservice.client.DreamStorageServiceClient
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.galleryofdreams.common.data.api.GalleryOfDreamsDataResponse
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionsManager
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.CommandMentions
import net.perfectdreams.loritta.cinnamon.discord.utils.*
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.dailytax.DailyTaxCollector
import net.perfectdreams.loritta.cinnamon.discord.utils.dailytax.DailyTaxWarner
import net.perfectdreams.loritta.cinnamon.discord.utils.directmessageprocessor.PendingImportantNotificationsProcessor
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
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.*
import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.morenitta.analytics.stats.LorittaStatsCollector
import net.perfectdreams.loritta.morenitta.christmas2022event.listeners.ReactionListener
import net.perfectdreams.loritta.morenitta.commands.CommandManager
import net.perfectdreams.loritta.morenitta.dao.*
import net.perfectdreams.loritta.morenitta.easter2023event.listeners.Easter2023ReactionListener
import net.perfectdreams.loritta.morenitta.interactions.InteractivityManager
import net.perfectdreams.loritta.morenitta.listeners.*
import net.perfectdreams.loritta.morenitta.listeners.PreStartGatewayEventReplayListener.Companion.FAKE_EVENT_FIELD
import net.perfectdreams.loritta.morenitta.loricoolcards.LoriCoolCardsManager
import net.perfectdreams.loritta.morenitta.modules.StarboardModule
import net.perfectdreams.loritta.morenitta.modules.WelcomeModule
import net.perfectdreams.loritta.morenitta.platform.discord.DiscordEmoteManager
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandMap
import net.perfectdreams.loritta.morenitta.platform.discord.utils.BucketedController
import net.perfectdreams.loritta.morenitta.platform.discord.utils.JVMLorittaAssets
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.raffles.LorittaRaffleTask
import net.perfectdreams.loritta.morenitta.threads.RemindersThread
import net.perfectdreams.loritta.morenitta.twitch.TwitchAPI
import net.perfectdreams.loritta.morenitta.twitch.TwitchSubscriptionsHandler
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.morenitta.utils.config.*
import net.perfectdreams.loritta.morenitta.utils.devious.*
import net.perfectdreams.loritta.morenitta.utils.ecb.ECBManager
import net.perfectdreams.loritta.morenitta.utils.giveaway.GiveawayManager
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.utils.metrics.Prometheus
import net.perfectdreams.loritta.morenitta.website.*
import net.perfectdreams.loritta.morenitta.websiteinternal.InternalWebServer
import net.perfectdreams.loritta.morenitta.youtube.CreateYouTubeWebhooksTask
import net.perfectdreams.loritta.serializable.*
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import net.perfectdreams.switchtwitch.SwitchTwitchAPI
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.jetbrains.exposed.sql.*
import java.io.File
import java.io.InputStream
import java.lang.reflect.Modifier
import java.nio.file.*
import java.security.SecureRandom
import java.sql.Connection
import java.time.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.io.path.*
import kotlin.math.ceil
import kotlin.reflect.KClass
import kotlin.system.exitProcess
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
	val initialSessions: Map<Int, GatewaySessionData>,
	val gatewayExtras: Map<Int, GatewayExtrasData>
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
	val ecbManager = ECBManager()

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
	val easter2023Listener = Easter2023ReactionListener(this)
	val giveawayInteractionsListener = GiveawayInteractionsListener(this)

	var builder: DefaultShardManagerBuilder

	lateinit var bomDiaECia: BomDiaECia

	var newWebsite: LorittaWebsite? = null
	var newWebsiteThread: Thread? = null

	var twitch = TwitchAPI(config.loritta.twitch.clientId, config.loritta.twitch.clientSecret)
	var switchTwitch = SwitchTwitchAPI(config.loritta.twitch.clientId, config.loritta.twitch.clientSecret)
	val twitchSubscriptionsHandler = TwitchSubscriptionsHandler(this)
	val connectionManager = ConnectionManager(this)
	var patchData = PatchData()
	var sponsors: List<Sponsor> = listOf()
	val cachedRetrievedArtists = CacheBuilder.newBuilder().expireAfterWrite(7, TimeUnit.DAYS)
		.build<Long, Optional<CachedUserInfo>>()
	var bucketedController: BucketedController? = null

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
	val discordSlashCommandScopeWorkaround = DiscordSlashCommandScopeWorkaround(this)

	val random = SecureRandom()
	val gifsicle = Gifsicle(config.loritta.binaries.gifsicle)

	val fanArtArtists = LorittaBot::class.getPathFromResources("/fan_arts_artists/")!!
		.let { Files.list(it).toList() }
		.map {
			loadFanArtArtist(it.inputStream())
		}

	val fanArts: List<FanArt>
		get() = fanArtArtists.flatMap { it.fanArts }

	/**
	 * Cached Gallery of Dreams Data response, used for fan art stuff
	 */
	var cachedGalleryOfDreamsDataResponse: GalleryOfDreamsDataResponse? = null

	/**
	 * Cached Gabriela Helper Merch Buyer IDs response, used for badges
	 */
	var cachedGabrielaHelperMerchBuyerIdsResponse: List<Long>? = null

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
	val starboardModule = StarboardModule(this)
	val activityUpdater = ActivityUpdater(this)
	val loriCoolCardsManager = LoriCoolCardsManager(this.graphicsFonts)

	// Stores if a gateway was successfully resumed during startup
	val gatewayShardsStartupResumeStatus = ConcurrentHashMap<Int, GatewayShardStartupResumeStatus>()

	private val internalWebServer = InternalWebServer(this)

	val preLoginStates = mutableMapOf<Int, MutableStateFlow<PreStartGatewayEventReplayListener.ProcessorState>>()
	var isActive = true

	// Used to lock raffle ticket purchases and raffle results
	val raffleResultsMutex = Mutex()

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

			if (state.value == PreStartGatewayEventReplayListener.ProcessorState.FINISHED) {
				// For these that are already FINISHED, they will always be LOGGED_IN_FROM_SCRATCH anyway
				gatewayShardsStartupResumeStatus[shardId] = GatewayShardStartupResumeStatus.LOGGED_IN_FROM_SCRATCH
			}

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
				bucketedController = BucketedController(this@LorittaBot, config.loritta.discord.maxConcurrency, config.loritta.discord.maxParallelLogins)
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
			.apply {
				val baseUrl = config.loritta.discord.baseUrl
				if (baseUrl != null) {
					logger.info { "Using Discord's base URL $baseUrl" }
					setRestConfig(RestConfig().setBaseUrl("${baseUrl.removeSuffix("/")}/api/v" + JDAInfo.DISCORD_REST_VERSION + "/"))
				}
			}
			.addEventListeners(
				discordListener,
				eventLogListener,
				messageListener,
				voiceChannelListener,
				gatewayRelayerListener,
				addReactionFurryAminoPtListener,
				boostGuildListener,
				interactionsListener,
				christmasListener,
				giveawayInteractionsListener,
				easter2023Listener
			)
			.addEventListenerProvider {
				PreStartGatewayEventReplayListener(
					this,
					initialSessions[it],
					gatewayExtras[it],
					cacheFolder,
					preLoginStates[it]!!,
				)
			}
	}

	val lorittaCluster: LorittaConfig.LorittaClustersConfig.LorittaClusterConfig
		get() {
			return config.loritta.clusters.instances.first { it.id == clusterId }
		}

	val lorittaMainCluster: LorittaConfig.LorittaClustersConfig.LorittaClusterConfig
		get() {
			return config.loritta.clusters.instances.first { it.id == 1 }
		}

	val lorittaInternalApiKey: LorittaConfig.WebsiteConfig.AuthenticationKey
		get() {
			return config.loritta.website.apiKeys.first { it.description == "Loritta Internal Key" }
		}

	val activeEvents = ConcurrentLinkedQueue<Job>()

	val prometheusPushClient = PrometheusPushClient("loritta-morenitta", config.loritta.prometheusPush.url)
	val voiceConnectionsManager = LorittaVoiceConnectionManager(this)

	val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	val tasksScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	// Used to workaround issues with users DoS'ing endpoints that require retrieveMember
	// The key is "GuildId#UserId"
	val cachedFailedMemberQueryResults = Caffeine.newBuilder()
		.expireAfterWrite(5L, TimeUnit.SECONDS)
		.build<String, Boolean>()
		.asMap()

	// Inicia a Loritta
	@OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class, ExperimentalSerializationApi::class,
		ExperimentalSerializationApi::class
	)
	fun start() {
		logger.info { "Starting Debug Web Server..." }
		internalWebServer.start()

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

		// Validate if we don't have more commands than Discord allows
		val slashCommandCount = interaKTions.manager.applicationCommandsDeclarations.filterIsInstance<SlashCommandDeclaration>().size + interactionsListener.manager.slashCommands.size

		if (slashCommandCount > 100) {
			logger.error { "Currently there are $slashCommandCount root commands registered, however Discord has a 100 root command limit! You need to remove some of the commands!" }
			exitProcess(1)
		}

		val userCommandCount = interaKTions.manager.applicationCommandsDeclarations.filterIsInstance<UserCommandDeclaration>().size + interactionsListener.manager.userCommands.size

		if (userCommandCount > 5) {
			logger.error { "Currently there are $slashCommandCount user commands registered, however Discord has a 5 user command limit! You need to remove some of the commands!" }
			exitProcess(1)
		}

		val messageCommandCount = interaKTions.manager.applicationCommandsDeclarations.filterIsInstance<MessageCommandDeclaration>().size + interactionsListener.manager.messageCommands.size

		if (messageCommandCount > 5) {
			logger.error { "Currently there are $slashCommandCount message commands registered, however Discord has a 5 message command limit! You need to remove some of the commands!" }
			exitProcess(1)
		}

		logger.info { "Total Root Commands: $slashCommandCount/100" }
		logger.info { "Total User Commands: $userCommandCount/5" }
		logger.info { "Total Message Commands: $messageCommandCount/5" }

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

		logger.info { "Starting Loritta tasks..." }
		startTasks()

		logger.info { "Sucesso! Iniciando threads da Loritta..." }

		logger.info { "Iniciando Tasks..." }
		val tasks = LorittaTasks(this)
		tasks.startTasks()

		logger.info { "Iniciando threads de reminders..." }
		RemindersThread(this).start()

		logger.info { "Iniciando bom dia & cia..." }
		bomDiaECia = BomDiaECia(this)

		// Ou seja, agora a Loritta está funcionando, Yay!
		Runtime.getRuntime().addShutdownHook(
			thread(false) {
				logger.info { "Shutting down Loritta... Bye bye!" }

				// Mark this as shutdown to avoid dispatching jobs
				isActive = false

				// Remove all event listeners to make Loritta not process new events while restarting
				shardManager.shards.forEach { shard ->
					shard.audioManagers.forEach {
						it.closeAudioConnection()
					}

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

				val limitedCount = System.getProperty("loritta.shutdownJobsParallelism", "32").toInt()
				logger.info { "Using $limitedCount limited parallism in Dispatcher.IO for shard shutdown" }

				measureTime {
					// Limit the shard saving stuff to X jobs in parallel
					val dispatcher = Dispatchers.IO.limitedParallelism(limitedCount)

					val shardJobs = shardManager
						.shards
						.sortedBy { it.shardInfo.shardId } // Sorted by shard ID just to be easier to track them down in the logs
						.map { shard ->
							GlobalScope.async(dispatcher) {
								measureTime {
									val jdaImpl = shard as JDAImpl
									val sessionId = jdaImpl.client.sessionId
									val resumeUrl = jdaImpl.client.resumeUrl
									val newLineUtf8 = "\n".toByteArray(Charsets.UTF_8)

									// Only get connected shards, invalidate everything else
									// The "shards.guildView.isEmpty" check is done to avoid shards with no guilds being saved to the disk, because shards without any guilds aren't correctly loaded by the code
									// This will never happen on Loritta (yay we have enough guilds for everyone!!) but it may happen when testing Loritta
									// So, to avoid someone debugging this without knowing wtf is happening, let's add that check in here
									if (shard.status != JDA.Status.CONNECTED || sessionId == null || resumeUrl == null || shard.guildsView.isEmpty) {
										logger.info { "Fully shutting down shard ${shard.shardInfo.shardId}..." }
										// Not connected, shut down and invalidate our cached data
										shard.shutdownNow(1000) // We don't care about persisting our gateway session
										File(cacheFolder, shard.shardInfo.shardId.toString()).deleteRecursively()
									} else {
										logger.info { "Shutting down shard ${shard.shardInfo.shardId} to be resumed later..." }
										val shutdownBeganAt = Clock.System.now()

										// Connected, store to the cache
										// Using close code 1012 does not invalidate your gateway session!
										shard.shutdownNow(1012)

										val shardCacheFolder = File(cacheFolder, shard.shardInfo.shardId.toString())

										// Delete the current cached data for this shard
										shardCacheFolder.deleteRecursively()

										// Create the shard cache folder
										shardCacheFolder.mkdirs()

										val guildsCacheFile = File(shardCacheFolder, "guilds.loriguilds.zst")
										val sessionCacheFile = File(shardCacheFolder, "session.json")
										val gatewayExtrasFile = File(shardCacheFolder, "extras.json")
										val versionFile = File(shardCacheFolder, "version")
										val deviousConverterVersionFile = File(shardCacheFolder, "deviousconverter_version")

										val guildIdsForReadyEvent = jdaImpl.guildsView.map { it.idLong } + jdaImpl.unavailableGuilds.map { it.toLong() }

										val guildCount = jdaImpl.guildsView.size()

										logger.info { "Trying to persist ${guildCount} guilds for shard ${jdaImpl.shardInfo.shardId}..." }

										val guildsToBePersistedByteArray = mutableListOf<ByteArray>()

										for (guild in jdaImpl.guildsView) {
											guild as GuildImpl

											val eventAsJson = """{"op":0,"d":${Json.encodeToString(DeviousConverter.toJson(guild))},"t":"GUILD_CREATE","$FAKE_EVENT_FIELD":true}""".toByteArray(Charsets.UTF_8)
											guildsToBePersistedByteArray.add(eventAsJson)

											// Remove the guild from memory, which avoids the bot crashing due to Out Of Memory
											guild.invalidate()
										}

										val compressedGuilds = Zstd.compress(
											ProtoBuf.encodeToByteArray(
												StoredGatewayGuilds(
													guildsToBePersistedByteArray
												)
											)
										)

										guildsCacheFile.writeBytes(compressedGuilds)

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

										val shutdownFinishedAt = Clock.System.now()
										gatewayExtrasFile
											.writeText(
												Json.encodeToString(
													GatewayExtrasData(
														shutdownBeganAt,
														shutdownFinishedAt
													)
												)
											)

										// Write the current DeviousConverter version...
										deviousConverterVersionFile.writeText(DeviousConverter.CACHE_VERSION.toString())

										// Only write after everything has been successfully written
										versionFile.writeText(connectionVersion.toString())
									}
								}.also { logger.info { "Took $it to process shard's ${shard.shardInfo.shardId} stuff!" } }
							}
						}

					runBlocking {
						shardJobs.awaitAll()
					}
				}.also { logger.info { "Took $it to persist all shards cache!! - Used $limitedCount limited parallism in Dispatcher.IO for shard shutdown" } }
			}
		)

		logger.info { "Yay! Loritta is up and running :3" }
	}

	fun initPostgreSql() {
		logger.info { "Starting PostgreSQL related things..." }

		logger.info { "Creating any missing tables and columns in PostgreSQL..." }
		runBlocking {
			pudding.createMissingTablesAndColumns { true }
		}
		logger.info { "Created all missing tables and columns!" }

		logger.info { "Updating Loritta trinkets..." }
		TrinketsStuff.updateTrinkets(pudding)
		logger.info { "Updated Loritta trinkets!" }
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

	suspend fun <T> transaction(transactionIsolation: Int = Connection.TRANSACTION_REPEATABLE_READ, statement: suspend Transaction.() -> T) = pudding.transaction(transactionIsolation = transactionIsolation) {
		statement.invoke(this)
	}

	suspend fun <T> newSuspendedTransaction(repetitions: Int = 5, transactionIsolation: Int = Connection.TRANSACTION_REPEATABLE_READ, statement: suspend Transaction.() -> T): T
			= pudding.transaction(repetitions, transactionIsolation, statement)

	suspend fun <T> suspendedTransactionAsync(statement: suspend Transaction.() -> T) = GlobalScope.async(coroutineDispatcher) {
		newSuspendedTransaction(statement = statement)
	}

	/**
	 * Loads the server configuration of a guild
	 *
	 * @param guildId the guild's ID
	 * @return        the server configuration
	 */
	suspend fun getOrCreateServerConfig(guildId: Long, loadFromCache: Boolean = false): ServerConfig {
		if (loadFromCache)
			cachedServerConfigs.getIfPresent(guildId)?.let { return it }

		return pudding.transaction {
			_getOrCreateServerConfig(guildId)
		}
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

		return GlobalScope.async { getOrCreateServerConfig(guildId) }
	}

	private fun _getOrCreateServerConfig(guildId: Long): ServerConfig {
		val result = ServerConfig.findById(guildId) ?: ServerConfig.new(guildId) {}
		cachedServerConfigs.put(guildId, result)
		return result
	}

	suspend fun getLorittaProfile(userId: String) = getLorittaProfile(userId.toLong())

	/**
	 * Loads the profile of an user
	 *
	 * @param userId the user's ID
	 * @return       the user profile
	 */
	suspend fun getLorittaProfile(userId: Long) = pudding.transaction { _getLorittaProfile(userId) }

	/**
	 * Loads the profile of an user deferred
	 *
	 * @param userId the user's ID
	 * @return       the user profile
	 */
	suspend fun getLorittaProfileDeferred(userId: Long) = GlobalScope.async { getLorittaProfile(userId) }

	fun _getLorittaProfile(userId: Long) = Profile.findById(userId)

	suspend fun getOrCreateLorittaProfile(userId: String) = getOrCreateLorittaProfile(userId.toLong())

	suspend fun getOrCreateLorittaProfile(userId: Long): Profile {
		val sqlProfile = transaction { Profile.findById(userId) }
		if (sqlProfile != null)
			return sqlProfile

		val profileSettings = transaction {
			// This fully qualified import is here because IDEA gets confused and thinks that it cannot import the class
			net.perfectdreams.loritta.morenitta.dao.ProfileSettings.new {
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

	suspend fun getActiveMoneyFromDonations(userId: Long): Double {
		return transaction { _getActiveMoneyFromDonations(userId) }
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

	/**
	 * Suspends until Loritta is connected to the specified audio channel
	 *
	 * @return if Loritta successfully connected to the voice channel
	 */
	suspend fun openAudioChannelAndAwaitConnection(audioManager: AudioManager, audioChannel: AudioChannel, timeout: Duration = 5.seconds): Boolean {
		// Already connected! Quickly fail then...
		if (audioManager.isConnected && audioManager.connectedChannel?.idLong == audioChannel.idLong)
			return true

		audioManager.openAudioConnection(audioChannel)

		val endChannel = Channel<Boolean>()

		// While we could use GuildVoiceUpdateEvent to track this, it seems that audioManager.connectedChannel is updated in a later stage, after GuildVoiceUpdateEvent has been triggered
		// So for now we use this polling mechanism
		val audioChannelCheckJob = GlobalScope.async {
			while (true) {
				val invalidChannel = !audioManager.isConnected || audioManager.connectedChannel?.idLong != audioChannel.idLong

				if (!invalidChannel) {
					// If it is the channel, send true to the channel
					// This will cause the code to exit the "withTimeout" block!
					endChannel.send(true)
					return@async
				}

				delay(250)
			}
		}

		return try {
			withTimeout(timeout) {
				endChannel.receive()
			}
			audioChannelCheckJob.cancel()
			true
		} catch (e: TimeoutCancellationException) {
			audioChannelCheckJob.cancel()
			false
		}
	}

	fun launchMessageJob(event: Event, block: suspend CoroutineScope.() -> Unit) {
		val coroutineName = when (event) {
			is MessageReceivedEvent -> "Message ${event.message} by user ${event.author} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is SlashCommandInteractionEvent -> "Slash Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is UserContextInteractionEvent -> "User Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is MessageContextInteractionEvent -> "User Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
			is CommandAutoCompleteInteractionEvent -> "Autocomplete for Command ${event.fullCommandName} by user ${event.user} in ${event.channel} on ${if (event.isFromGuild) event.guild else null}"
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

	suspend fun getCachedUserInfo(userId: UserId): net.perfectdreams.loritta.serializable.CachedUserInfo? {
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
				null,
				restUser.avatar
			)

			return net.perfectdreams.loritta.serializable.CachedUserInfo(
				UserId(restUser.id.value),
				restUser.username,
				restUser.discriminator,
				null,
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
			null,
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
	 * Schedules [action] to be executed on [tasksScope] every [period] with a [initialDelay]
	 */
	private fun scheduleCoroutineAtFixedRate(
		taskName: String,
		period: Duration,
		initialDelay: Duration = Duration.ZERO,
		action: RunnableCoroutine
	) {
		logger.info { "Scheduling ${action::class.simpleName} to be ran every $period with a $initialDelay initial delay" }
		scheduleCoroutineAtFixedRate(taskName, tasksScope, period, initialDelay, action)
	}

	/**
	 * Schedules [action] to be executed on [tasksScope] every [period] with a [initialDelay] if this [isMainReplica]
	 */
	private fun scheduleCoroutineAtFixedRateIfMainReplica(
		taskName: String,
		period: Duration,
		initialDelay: Duration = Duration.ZERO,
		action: RunnableCoroutine
	) {
		if (isMainInstance)
			scheduleCoroutineAtFixedRate(taskName, period, initialDelay, action)
	}

	private fun scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(taskName: String, time: LocalTime, action: RunnableCoroutine) {
		val now = Instant.now()
		val today = LocalDate.now(ZoneOffset.UTC)
		val todayAtTime = LocalDateTime.of(today, time)
		val gonnaBeScheduledAtTime =  if (now > todayAtTime.toInstant(ZoneOffset.UTC)) {
			// If today at time is larger than today, then it means that we need to schedule it for tomorrow
			todayAtTime.plusDays(1)
		} else todayAtTime

		val diff = gonnaBeScheduledAtTime.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

		scheduleCoroutineAtFixedRateIfMainReplica(
			taskName,
			1.days,
			diff.milliseconds,
			action
		)
	}

	private fun startTasks() {
		scheduleCoroutineAtFixedRateIfMainReplica(PendingImportantNotificationsProcessor::class.simpleName!!, 1.seconds, action = PendingImportantNotificationsProcessor(this@LorittaBot))
		scheduleCoroutineAtFixedRateIfMainReplica(LorittaStatsCollector::class.simpleName!!, 1.minutes, action = LorittaStatsCollector(this@LorittaBot))
		scheduleCoroutineAtFixedRateIfMainReplica(CreateYouTubeWebhooksTask::class.simpleName!!, 1.minutes, action = CreateYouTubeWebhooksTask(this@LorittaBot))
		scheduleCoroutineAtFixedRateIfMainReplica(LorittaRaffleTask::class.simpleName!!, 1.seconds, action = LorittaRaffleTask(this@LorittaBot))
		scheduleCoroutineAtFixedRateIfMainReplica(BotVotesNotifier::class.simpleName!!, 1.minutes, action = BotVotesNotifier(this))
		scheduleCoroutineAtFixedRateIfMainReplica(TwitchSubscriptionsHandler::class.simpleName!!, 15.minutes) {
			// Just request it to be executed
			twitchSubscriptionsHandler.requestSubscriptionCreation("Periodic Subscriptions Updater")
		}
		scheduleCoroutineAtFixedRate(ActivityUpdater::class.simpleName!!, 1.minutes, action = activityUpdater)
		GlobalScope.launch(CoroutineName("Create Twitch Subscriptions Loop")) {
			twitchSubscriptionsHandler.createSubscriptionsLoop()
		}

		// Update Fan Arts
		scheduleCoroutineAtFixedRate("GalleryOfDreamsFanArtsUpdater", 1.minutes) {
			try {
				logger.info { "Updating Fan Arts..." }
				val response = http.get("https://fanarts.perfectdreams.net/api/v1/fan-arts")

				if (response.status != HttpStatusCode.OK) {
					logger.warn { "Gallery of Dreams' Get Fan Arts API response was ${response.status}!" }
					return@scheduleCoroutineAtFixedRate
				}

				val payload = response.bodyAsText(Charsets.UTF_8)
				val galleryOfDreamsDataResponse = Json.decodeFromString<GalleryOfDreamsDataResponse>(payload)

				this.cachedGalleryOfDreamsDataResponse = galleryOfDreamsDataResponse
			} catch (e: Exception) {
				logger.warn(e) { "Failed to get illustrators' information from GalleryOfDreams!" }
			}
		}

		// Update Merch Buyers
		scheduleCoroutineAtFixedRate("GabrielaHelperMerchBuyersUpdater", 1.minutes) {
			try {
				logger.info { "Updating Merch Buyers..." }
				val response = http.get("${config.loritta.gabrielaHelperService.url.removeSuffix("/")}/api/user-ids-that-have-purchased-something")

				if (response.status != HttpStatusCode.OK) {
					logger.warn { "Gabriela Helper' User IDs That Have Purchased Something API response was ${response.status}!" }
					return@scheduleCoroutineAtFixedRate
				}

				val payload = response.bodyAsText(Charsets.UTF_8)

				this.cachedGabrielaHelperMerchBuyerIdsResponse = Json.parseToJsonElement(payload).jsonArray.map { it.jsonPrimitive.long }
			} catch (e: Exception) {
				logger.warn(e) { "Failed to get merch buyer IDs from Gabriela Helper!" }
			}
		}

		val dailyTaxWarner = DailyTaxWarner(this)
		val dailyTaxCollector = DailyTaxCollector(this)

		// 12 hours before
		scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(
			DailyTaxWarner::class.simpleName!!,
			LocalTime.of(12, 0),
			dailyTaxWarner
		)

		// 4 hours before
		scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(
			DailyTaxWarner::class.simpleName!!,
			LocalTime.of(20, 0),
			dailyTaxWarner
		)

		// 1 hour before
		scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(
			DailyTaxWarner::class.simpleName!!,
			LocalTime.of(23, 0),
			dailyTaxWarner
		)

		// at midnight + notify about the user about taxes
		scheduleCoroutineEveryDayAtSpecificHourIfMainReplica(
			DailyTaxCollector::class.simpleName!!,
			LocalTime.MIDNIGHT,
			dailyTaxCollector
		)
	}

	suspend inline fun <reified T : LorittaInternalRPCResponse> makeRPCRequest(
		cluster: LorittaConfig.LorittaClustersConfig.LorittaClusterConfig,
		rpc: LorittaInternalRPCRequest
	): T {
		return Json.decodeFromString<LorittaInternalRPCResponse>(
			http.post("${cluster.rpcUrl.removeSuffix("/")}/rpc") {
				setBody(Json.encodeToString<LorittaInternalRPCRequest>(rpc))
			}.bodyAsText()
		) as T
	}

	suspend fun loadActivity(): ActivityUpdater.ActivityWrapper? {
		val currentActiveLorittaAvatarRR = transaction {
			FanArtsExtravaganza.select {
				FanArtsExtravaganza.active eq true and (FanArtsExtravaganza.enabled eq true)
			}.firstOrNull()
		}

		if (currentActiveLorittaAvatarRR == null) {
			logger.warn { "There isn't a default avatar set for the Fan Art Extravaganza! Using default activity as a fallback... Please create a entry in the ${FanArtsExtravaganza.tableName} table with the \"${FanArtsExtravaganza.defaultAvatar.name}\" field set to true!" }
		}

		if (currentActiveLorittaAvatarRR == null || currentActiveLorittaAvatarRR[FanArtsExtravaganza.defaultAvatar]) {
			// Default avatar, use the default activity
			val now = Instant.now()
			val gatewayActivity = newSuspendedTransaction {
				GatewayActivities.select {
					GatewayActivities.startsAt lessEq Instant.now() and (GatewayActivities.endsAt greaterEq now)
				}.orderBy(Pair(GatewayActivities.startsAt, SortOrder.DESC), Pair(GatewayActivities.priority, SortOrder.DESC))
					.limit(1)
					.firstOrNull()
			} ?: return null

			val text = gatewayActivity[GatewayActivities.text]
			val type = Activity.ActivityType.valueOf(gatewayActivity[GatewayActivities.type])
			val streamUrl = gatewayActivity[GatewayActivities.streamUrl]

			return ActivityUpdater.ActivityWrapper(
				text,
				type,
				streamUrl
			)
		} else {
			return ActivityUpdater.ActivityWrapper(
				"\uD83C\uDFA8 Fan Art by ${currentActiveLorittaAvatarRR[FanArtsExtravaganza.artistName]}",
				Activity.ActivityType.CUSTOM_STATUS,
				null
			)
		}
	}

	fun createActivityText(activityText: String, shardId: Int) = "$activityText | Cluster ${lorittaCluster.id} [$shardId]"
}