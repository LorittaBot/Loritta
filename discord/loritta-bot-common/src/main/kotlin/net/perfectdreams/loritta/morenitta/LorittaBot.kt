package net.perfectdreams.loritta.morenitta

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.*
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import net.perfectdreams.loritta.morenitta.commands.CommandManager
import net.perfectdreams.loritta.morenitta.listeners.*
import net.perfectdreams.loritta.morenitta.tables.*
import net.perfectdreams.loritta.morenitta.tables.Dailies
import net.perfectdreams.loritta.morenitta.tables.Marriages
import net.perfectdreams.loritta.morenitta.tables.Profiles
import net.perfectdreams.loritta.morenitta.tables.ShipEffects
import net.perfectdreams.loritta.morenitta.tables.StarboardMessages
import net.perfectdreams.loritta.morenitta.tables.UserSettings
import net.perfectdreams.loritta.morenitta.threads.RaffleThread
import net.perfectdreams.loritta.morenitta.threads.RemindersThread
import net.perfectdreams.loritta.morenitta.threads.UpdateStatusThread
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.perfectdreams.dreamstorageservice.client.DreamStorageServiceClient
import net.perfectdreams.loritta.cinnamon.discord.utils.ecb.ECBManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Background
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.BotVotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.exposed.tables.CachedDiscordWebhooks
import net.perfectdreams.loritta.common.locale.LocaleManager
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
import net.perfectdreams.loritta.morenitta.dao.*
import net.perfectdreams.loritta.morenitta.modules.WelcomeModule
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandMap
import net.perfectdreams.loritta.morenitta.platform.discord.utils.GuildSetupQueue
import net.perfectdreams.loritta.morenitta.platform.discord.utils.JVMLorittaAssets
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.utils.ProcessDiscordGatewayCommands
import net.perfectdreams.loritta.morenitta.utils.Sponsor
import net.perfectdreams.loritta.morenitta.utils.config.*
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import net.perfectdreams.loritta.morenitta.utils.giveaway.GiveawayManager
import net.perfectdreams.loritta.morenitta.utils.locale.Gender
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.utils.metrics.Prometheus
import net.perfectdreams.loritta.morenitta.utils.payments.PaymentReason
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import redis.clients.jedis.JedisPool
import java.awt.image.BufferedImage
import java.io.File
import java.lang.reflect.Modifier
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.SecureRandom
import java.sql.Connection
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.math.ceil

/**
 * Loritta's main class, where everything (and anything) can happen!
 *
 * @author MrPowerGamerBR
 */
class LorittaBot(
	val discordConfig: GeneralDiscordConfig,
	val discordInstanceConfig: GeneralDiscordInstanceConfig,
	val config: GeneralConfig,
	val instanceConfig: GeneralInstanceConfig,
	val pudding: Pudding,
	val jedisPool: JedisPool
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
		var LOCALES = "/home/servers/loritta/locales/" // Pasta usada para as locales
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

	// ===[ LORITTA ]===
	var lorittaShards = LorittaShards(this) // Shards da Loritta
	val webhookExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), ThreadFactoryBuilder().setNameFormat("Webhook Sender %d").build())
	val webhookOkHttpClient = OkHttpClient()

	val legacyCommandManager = CommandManager(this) // Nosso command manager
	var messageInteractionCache = Caffeine.newBuilder().maximumSize(1000L).expireAfterAccess(3L, TimeUnit.MINUTES).build<Long, MessageInteractionFunctions>().asMap()

	var ignoreIds = mutableSetOf<Long>() // IDs para serem ignorados nesta sessão
	val apiCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS).maximumSize(100).build<String, Long>().asMap()

	var discordListener = DiscordListener(this) // Vamos usar a mesma instância para todas as shards
	var eventLogListener = EventLogListener(this) // Vamos usar a mesma instância para todas as shards
	var messageListener = MessageListener(this)
	var voiceChannelListener = VoiceChannelListener(this)
	var discordMetricsListener = DiscordMetricsListener(this)
	val gatewayRelayerListener = GatewayEventRelayerListener(this)
	val addReactionFurryAminoPtListener = AddReactionFurryAminoPtListener(this)
	var builder: DefaultShardManagerBuilder

	lateinit var raffleThread: RaffleThread
	lateinit var bomDiaECia: BomDiaECia

	var newWebsite: net.perfectdreams.loritta.morenitta.website.LorittaWebsite? = null
	var newWebsiteThread: Thread? = null

	var twitch = TwitchAPI(config.twitch.clientId, config.twitch.clientSecret)
	val connectionManager = ConnectionManager(this)
	var patchData = PatchData()
	var sponsors: List<Sponsor> = listOf()
	val cachedRetrievedArtists = CacheBuilder.newBuilder().expireAfterWrite(7, TimeUnit.DAYS)
		.build<Long, Optional<CachedUserInfo>>()
	var bucketedController: BucketedController? = null
	val rateLimitChecker = RateLimitChecker(this)

	var pendingGatewayEventsCount = 0L

	val perfectPaymentsClient = PerfectPaymentsClient(config.perfectPayments.url)

	val commandMap = DiscordCommandMap(this)
	val assets = JVMLorittaAssets(this)
	val localeManager = LocaleManager(File(instanceConfig.loritta.folders.locales))
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
		config.dreamStorageService.url,
		config.dreamStorageService.token,
		httpWithoutTimeout
	)

	val random = SecureRandom()

	var fanArtArtists = listOf<FanArtArtist>()
	val fanArts: List<FanArt>
		get() = fanArtArtists.flatMap { it.fanArts }
	val profileDesignManager = ProfileDesignManager(this)

	val isMaster: Boolean
		get() {
			return instanceConfig.loritta.currentClusterId == 1L
		}

	val cachedServerConfigs = Caffeine.newBuilder()
		.maximumSize(config.caches.serverConfigs.maximumSize)
		.expireAfterWrite(config.caches.serverConfigs.expireAfterWrite, TimeUnit.SECONDS)
		.build<Long, ServerConfig>()

	// Used for message execution
	val coroutineMessageExecutor = createThreadPool("Message Executor Thread %d")
	val coroutineMessageDispatcher = coroutineMessageExecutor.asCoroutineDispatcher() // Coroutine Dispatcher

	val coroutineExecutor = createThreadPool("Coroutine Executor Thread %d")
	val coroutineDispatcher = coroutineExecutor.asCoroutineDispatcher() // Coroutine Dispatcher
	fun createThreadPool(name: String) = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat(name).build())

	val pendingMessages = ConcurrentLinkedQueue<Job>()
	val guildSetupQueue = GuildSetupQueue(this)
	val commandCooldownManager = CommandCooldownManager(this)
	val giveawayManager = GiveawayManager(this)
	val welcomeModule = WelcomeModule(this)
	val ecbManager = ECBManager()

	fun redisKey(key: String) = "${config.redis.keyPrefix}:$key"

	init {
		FOLDER = instanceConfig.loritta.folders.root
		ASSETS = instanceConfig.loritta.folders.assets
		TEMP = instanceConfig.loritta.folders.temp
		LOCALES = instanceConfig.loritta.folders.locales
		FRONTEND = instanceConfig.loritta.website.folder

		val dispatcher = Dispatcher()
		dispatcher.maxRequestsPerHost = discordConfig.discord.maxRequestsPerHost

		val okHttpBuilder = OkHttpClient.Builder()
			.dispatcher(dispatcher)
			.connectTimeout(discordConfig.okHttp.connectTimeout, TimeUnit.SECONDS) // O padrão de timeouts é 10 segundos, mas vamos aumentar para evitar problemas.
			.readTimeout(discordConfig.okHttp.readTimeout, TimeUnit.SECONDS)
			.writeTimeout(discordConfig.okHttp.writeTimeout, TimeUnit.SECONDS)
			.protocols(listOf(Protocol.HTTP_1_1)) // https://i.imgur.com/FcQljAP.png
			.apply {
				if (discordConfig.okHttp.proxyUrl != null) {
					val split = discordConfig.okHttp.proxyUrl.split(":")
					this.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(split[0], split[1].toInt())))
				}
			}


		builder = DefaultShardManagerBuilder.create(discordConfig.discord.clientToken, discordConfig.discord.intents)
			// By default all flags are enabled, so we disable all flags and then...
			.disableCache(CacheFlag.values().toList())
			.enableCache(discordConfig.discord.cacheFlags) // ...we enable all the flags again
			.setChunkingFilter(ChunkingFilter.NONE) // No chunking policy because trying to load all members is hard
			.setMemberCachePolicy(MemberCachePolicy.ALL) // Cache all members!!
			.apply {
				if (discordConfig.shardController.enabled) {
					logger.info { "Using shard controller (for bots with \"sharding for very large bots\" to manage shards!" }
					bucketedController = BucketedController(this@LorittaBot, discordConfig.shardController.buckets)
					this.setSessionController(bucketedController)
				}
			}
			.setShardsTotal(discordConfig.discord.maxShards)
			.setShards(lorittaCluster.minShard.toInt(), lorittaCluster.maxShard.toInt())
			.setStatus(discordConfig.discord.status)
			.setBulkDeleteSplittingEnabled(false)
			.setHttpClientBuilder(okHttpBuilder)
			.setRawEventsEnabled(true)
			.setActivityProvider {
				// Before we updated the status every 60s and rotated between a list of status
				// However this causes issues, Discord blocks all gateway events until the status is
				// updated in all guilds in the shard she is in, which feels... bad, because it takes
				// long for her to reply to new messages.

				// Used to display the current Loritta cluster in the status
				val currentCluster = this.lorittaCluster

				Activity.of(
					Activity.ActivityType.valueOf(discordConfig.discord.activity.type),
					"${discordConfig.discord.activity.name} | Cluster ${currentCluster.id} [$it]"
				)
			}
			.addEventListeners(
				discordListener,
				eventLogListener,
				messageListener,
				voiceChannelListener,
				discordMetricsListener,
				gatewayRelayerListener,
				addReactionFurryAminoPtListener
			)
	}

	val lorittaCluster: GeneralConfig.LorittaClusterConfig
		get() {
			return config.clusters.first { it.id == instanceConfig.loritta.currentClusterId }
		}

	val lorittaInternalApiKey: GeneralConfig.LorittaConfig.WebsiteConfig.AuthenticationKey
		get() {
			return config.loritta.website.apiKeys.first { it.description == "Loritta Internal Key" }
		}

	// Inicia a Loritta
	fun start() {
		logger.info { "Registering Prometheus Collectors..." }
		Prometheus.register()

		logger.info { "Success! Creating folders..." }
		File(FOLDER).mkdirs()
		File(ASSETS).mkdirs()
		File(TEMP).mkdirs()
		File(LOCALES).mkdirs()
		File(FRONTEND).mkdirs()
		File(this.instanceConfig.loritta.folders.plugins).mkdirs()
		File(this.instanceConfig.loritta.folders.fanArts).mkdirs()

		logger.info { "Success! Loading locales..." }

		localeManager.loadLocales()
		loadLegacyLocales()

		logger.info { "Success! Loading fan arts..." }
		if (this.isMaster) // Apenas o master cluster deve carregar as fan arts, os outros clusters irão carregar pela API
			loadFanArts()

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

		// Vamos criar todas as instâncias necessárias do JDA para nossas shards
		logger.info { "Sucesso! Iniciando Loritta (Discord Bot)..." }

		val shardManager = builder.build()
		lorittaShards.shardManager = shardManager

		logger.info { "Sucesso! Iniciando plugins da this..." }

		logger.info { "Sucesso! Iniciando threads da this..." }

		logger.info { "Iniciando Update Status Thread..." }
		UpdateStatusThread(this).start() // Iniciar thread para atualizar o status da Loritta

		logger.info { "Iniciando Tasks..." }
		val tasks = LorittaTasks(this)
		tasks.startTasks()

		logger.info { "Iniciando threads de reminders..." }
		RemindersThread(this).start()

		logger.info { "Iniciando bom dia & cia..." }
		bomDiaECia = BomDiaECia(this)

		if (this.isMaster) {
			logger.info { "Loading raffle..." }
			val raffleFile = File(FOLDER, "raffle.json")

			if (raffleFile.exists()) {
				logger.info { "Parsing the JSON object..." }
				val json = JsonParser.parseString(raffleFile.readText()).obj

				logger.info { "Loaded raffle data! ${RaffleThread.started}; ${json["lastWinnerId"].nullString}; ${json["lastWinnerPrize"].nullInt}" }
				RaffleThread.started = json["started"].long
				RaffleThread.lastWinnerId = json["lastWinnerId"].nullLong
				RaffleThread.lastWinnerPrize = json["lastWinnerPrize"].nullInt ?: 0
				val userIdArray = json["userIds"].nullArray

				if (userIdArray != null) {
					logger.info { "Loading ${userIdArray.size()} raffle user entries..." }
					val firstUserIdEntry = userIdArray.firstOrNull()
					if (firstUserIdEntry != null) {
						if (firstUserIdEntry.isJsonObject && firstUserIdEntry.asJsonObject.has("second")) {
							// Old code
							logger.info { "Loading directly from the JSON array, using the \"first\" property value..." }
							val data = userIdArray.map { it["first"].long }
							RaffleThread.userIds.addAll(data)
						} else {
							logger.info { "Loading directly from the JSON array..." }
							RaffleThread.userIds.addAll(userIdArray.map { it.long })
						}
					}
				}
			}

			RaffleThread.isReady = true
			raffleThread = RaffleThread(this)
			raffleThread.start()
		}

		DebugLog.startCommandListenerThread(this)

		// Ou seja, agora a Loritta está funcionando, Yay!

		Thread(
			ProcessDiscordGatewayCommands(this, jedisPool),
			"Loritta Gateway Commands Processor Notification Listener"
		).start()
	}

	fun initPostgreSql() {
		logger.info("Iniciando PostgreSQL...")

		// Hidden behind a env flag, because FOR SOME REASON Exposed thinks that it is a good idea to
		// "ALTER TABLE serverconfigs ALTER COLUMN prefix TYPE TEXT, ALTER COLUMN prefix SET DEFAULT '+'"
		// And that LOCKS the ServerConfig table, and sometimes that takes a LOOOONG time to complete, which locks up everything
		if (System.getenv("LORITTA_CREATE_TABLES") != null) {
			runBlocking {
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

		// TrinketsStuff.updateTrinkets(pudding)
	}

	fun startWebServer() {
		// Carregar os blog posts
		newWebsiteThread = thread(true, name = "Website Thread") {
			val nWebsite = net.perfectdreams.loritta.morenitta.website.LorittaWebsite(this, instanceConfig.loritta.website.url, instanceConfig.loritta.website.folder)
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
					return "${this.config.dreamStorageService.url}/$dssNamespace/${StoragePaths.CustomBackground(userId, file).join()}.$extension"
				}
			}

			// If everything fails, change the background to the default blue background
			// This is required because the current background is "CUSTOM", so Loritta will try getting the default variation of the custom background...
			// but that doesn't exist!
			background = defaultBlueBackground
		}

		val dssNamespace = dreamStorageService.getCachedNamespaceOrRetrieve()
		val variation = background.getVariationForProfileDesign(activeProfileDesignInternalName)
		return getBackgroundUrlWithCropParameters(this.config.dreamStorageService.url, dssNamespace, variation)
	}

	private fun getBackgroundUrl(
		dreamStorageServiceUrl: String,
		namespace: String,
		background: BackgroundVariation
	): String {
		val extension = MediaTypeUtils.convertContentTypeToExtension(background.preferredMediaType)
		return "$dreamStorageServiceUrl/$namespace/${StoragePaths.Background(background.file).join()}.$extension"
	}

	private fun getBackgroundUrlWithCropParameters(
		dreamStorageServiceUrl: String,
		namespace: String,
		variation: BackgroundVariation
	): String {
		var url = getBackgroundUrl(dreamStorageServiceUrl, namespace, variation)
		val crop = variation.crop
		if (crop != null)
			url += "?crop_x=${crop.x}&crop_y=${crop.y}&crop_width=${crop.width}&crop_height=${crop.height}"
		return url
	}

	/**
	 * Loads the artists from the Fan Arts folder
	 *
	 * In the future this will be loaded from Loritta's website!
	 */
	fun loadFanArts() {
		val f = File(instanceConfig.loritta.folders.fanArts)

		fanArtArtists = f.listFiles().filter { it.extension == "conf" }.map {
			loadFanArtArtist(it)
		}
	}

	/**
	 * Loads an specific fan art artist
	 */
	fun loadFanArtArtist(file: File): FanArtArtist = Constants.HOCON_MAPPER.readValue(file)

	fun getFanArtArtistByFanArt(fanArt: FanArt) = fanArtArtists.firstOrNull { fanArt in it.fanArts }

	/**
	 * Initializes the available locales and adds missing translation strings to non-default languages
	 *
	 * @see LegacyBaseLocale
	 */
	fun loadLegacyLocales() {
		val locales = mutableMapOf<String, LegacyBaseLocale>()

		val legacyLocalesFolder = File(instanceConfig.loritta.folders.locales, "legacy")

		// Carregar primeiro o locale padrão
		val defaultLocaleFile = File(legacyLocalesFolder, "default.json")
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
		for (file in localesFolder.listFiles()) {
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

				val localeFile = File(legacyLocalesFolder, "$id.json")
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

				File(legacyLocalesFolder, "$id.json").writeText(prettyGson.toJson(jsonObject))
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

		if (this.config.caches.serverConfigs.maximumSize != 0L)
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
				gender = Gender.UNKNOWN
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
			is GuildMessageReceivedEvent -> {
				"Message ${event.message} by user ${event.author} in ${event.channel} on ${event.guild}"
			}
			is PrivateMessageReceivedEvent -> {
				"Message ${event.message} by user ${event.author} in ${event.channel}"
			}
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
}