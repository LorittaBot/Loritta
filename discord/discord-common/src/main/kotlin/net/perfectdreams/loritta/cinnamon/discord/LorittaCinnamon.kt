package net.perfectdreams.loritta.cinnamon.discord

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.request.StackTraceRecoveringKtorRequestHandler
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.DiscordInteraKTions
import net.perfectdreams.dreamstorageservice.client.DreamStorageServiceClient
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.gateway.modules.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionsManager
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.CommandMentions
import net.perfectdreams.loritta.cinnamon.discord.utils.*
import net.perfectdreams.loritta.cinnamon.discord.utils.config.CinnamonConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosPackageInfoUpdater
import net.perfectdreams.loritta.cinnamon.discord.utils.dailytax.DailyTaxCollector
import net.perfectdreams.loritta.cinnamon.discord.utils.dailytax.DailyTaxWarner
import net.perfectdreams.loritta.cinnamon.discord.utils.directmessageprocessor.PendingImportantNotificationsProcessor
import net.perfectdreams.loritta.cinnamon.discord.utils.ecb.ECBManager
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.DiscordCacheService
import net.perfectdreams.loritta.cinnamon.discord.utils.falatron.Falatron
import net.perfectdreams.loritta.cinnamon.discord.utils.falatron.FalatronModelsManager
import net.perfectdreams.loritta.cinnamon.discord.utils.google.GoogleVisionOCRClient
import net.perfectdreams.loritta.cinnamon.discord.utils.google.HackyGoogleTranslateClient
import net.perfectdreams.loritta.cinnamon.discord.utils.images.EmojiImageCache
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImage
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.DiscordGatewayEventsProcessorMetrics
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.PrometheusPushClient
import net.perfectdreams.loritta.cinnamon.discord.utils.profiles.ProfileDesignManager
import net.perfectdreams.loritta.cinnamon.discord.utils.soundboard.Soundboard
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnectionManager
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Background
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.CustomBackgroundSettings
import net.perfectdreams.loritta.cinnamon.pudding.utils.LorittaNotificationListener
import net.perfectdreams.loritta.cinnamon.utils.UserPremiumPlans
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import org.jetbrains.exposed.sql.select
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Transaction
import java.awt.image.BufferedImage
import java.security.SecureRandom
import java.time.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/**
 * Represents a Loritta Morenitta (Cinnamon) implementation.
 */
class LorittaCinnamon(
    /**
     * Gets if this replica is the main replica.
     */
    val isMainReplica: Boolean,
    val gatewayManager: LorittaDiscordGatewayManager,
    val config: CinnamonConfig,

    val languageManager: LanguageManager,
    services: Pudding,
    val jedisPool: JedisPool,
    val redisKeys: RedisKeys,
    val http: HttpClient
) : LorittaDiscordStuff(config.discord, services) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @OptIn(KordExperimental::class)
    val kord = Kord.restOnly(config.discord.token) {
        requestHandler {
            StackTraceRecoveringKtorRequestHandler(KtorRequestHandler(it.token))
        }
    }

    val cache = DiscordCacheService(this)

    val interaKTions = DiscordInteraKTions(
        kord,
        Snowflake(discordConfig.applicationId)
    )

    val interactionsManager = InteractionsManager(
        this,
        interaKTions
    )

    val gabrielaImageServerClient = GabrielaImageServerClient(
        config.services.gabrielaImageServer.url,
        HttpClient {
            // Increase the default timeout for image generation, because some video generations may take too long to be generated
            install(HttpTimeout) {
                this.socketTimeoutMillis = 60_000
                this.requestTimeoutMillis = 60_000
                this.connectTimeoutMillis = 60_000
            }
        }
    )
    val dreamStorageService = DreamStorageServiceClient(
        config.services.dreamStorageService.url,
        config.services.dreamStorageService.token,
        HttpClient {
            install(HttpTimeout) {
                this.socketTimeoutMillis = 60_000
                this.requestTimeoutMillis = 60_000
                this.connectTimeoutMillis = 60_000
            }
        }
    )

    val mojangApi = MinecraftMojangAPI()
    val correiosClient = CorreiosClient()
    val randomRoleplayPicturesClient = RandomRoleplayPicturesClient(config.services.randomRoleplayPictures.url)
    val falatronModelsManager = FalatronModelsManager().also {
        it.startUpdater()
    }
    val ecbManager = ECBManager()
    val falatron = Falatron(config.falatron.url, config.falatron.key)
    val soundboard = Soundboard()
    val profileDesignManager = ProfileDesignManager(this)
    // TODO: This is very hacky, maybe this could be improved somehow?
    lateinit var commandMentions: CommandMentions
    val unicodeEmojiManager = UnicodeEmojiManager()
    val emojiImageCache = EmojiImageCache()
    val graphicsFonts = GraphicsFonts()
    val googleTranslateClient = HackyGoogleTranslateClient()
    val googleVisionOCRClient = GoogleVisionOCRClient(config.services.googleVision.key)

    val random = SecureRandom()

    val activeEvents = ConcurrentLinkedQueue<Job>()

    val prometheusPushClient = PrometheusPushClient("loritta-cinnamon", config.prometheusPush.url)

    val voiceConnectionsManager = LorittaVoiceConnectionManager(this)

    private val starboardModule = StarboardModule(this)
    private val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)
    private val discordCacheModule = DiscordCacheModule(this)
    private val bomDiaECiaModule = BomDiaECiaModule(this)
    private val debugGatewayModule = DebugGatewayModule(this)
    private val owoGatewayModule = OwOGatewayModule(this)
    private val inviteBlockerModule = InviteBlockerModule(this)
    private val afkModule = AFKModule(this)

    private val scope = CoroutineScope(Dispatchers.Default)

    private val debugWebServer = DebugWebServer()

    // This is executed sequentially!
    val modules = listOf(
        discordCacheModule,
        inviteBlockerModule,
        afkModule,
        addFirstToNewChannelsModule,
        starboardModule,
        owoGatewayModule,
        debugGatewayModule
    )

    val notificationListener = LorittaNotificationListener(services.hikariDataSource)
        .apply {
            this.start()
        }

    val analyticHandlers = mutableListOf<EventAnalyticsTask.AnalyticHandler>()
    val cinnamonTasks = CinnamonTasks(this)
    val tasksScope = CoroutineScope(Dispatchers.Default)

    fun start() {
        runBlocking {
            logger.info { "Starting Debug Web Server..." }
            debugWebServer.start()

            val tableNames = config.services.pudding.tablesAllowedToBeUpdated
            services.createMissingTablesAndColumns {
                if (tableNames == null)
                    true
                else it in tableNames
            }
            // TODO: Fix this
            // TrinketsStuff.updateTrinkets(services)

            logger.info { "Starting Pudding tasks..." }
            services.startPuddingTasks()

            logger.info { "Registering interactions features..." }
            interactionsManager.register()

            logger.info { "Starting Cinnamon tasks..." }
            cinnamonTasks.start()
            startTasks()

            // On every gateway instance present on our gateway manager, collect and process events
            logger.info { "Preparing gateway event collectors for ${gatewayManager.gateways.size} gateway instances..." }
            gatewayManager.gateways.forEach { (shardId, gateway) ->
                scope.launch {
                    gateway.events.collect {
                        DiscordGatewayEventsProcessorMetrics.gatewayEventsReceived
                            .labels(shardId.toString(), it::class.simpleName ?: "Unknown")
                            .inc()

                        launchEventProcessorJob(
                            GatewayEventContext(
                                it,
                                shardId,
                                Clock.System.now()
                            )
                        )
                    }
                }
            }
            logger.info { "Loritta Cinnamon is now up and running! :3" }
        }
    }

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
    private fun launchEventProcessorJob(context: GatewayEventContext) {
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
        services,
        rest,
        userId,
        builder
    )

    /**
     * Filters received notifications by their [notificationUniqueId]
     *
     * @param notificationUniqueId the notification unique ID
     * @return a flow containing only notifications that match the unique ID
     */
    fun filterNotificationsByUniqueId(notificationUniqueId: String): Flow<LorittaNotification> {
        return notificationListener.notifications.filterIsInstance<LorittaNotification>()
            .filter { it.uniqueId == notificationUniqueId }
    }

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
        if (isMainReplica)
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
        scheduleCoroutineAtFixedRateIfMainReplica(15.seconds, action = CorreiosPackageInfoUpdater(this@LorittaCinnamon))
        scheduleCoroutineAtFixedRateIfMainReplica(1.seconds, action = PendingImportantNotificationsProcessor(this@LorittaCinnamon))

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
            // TODO: Hostname somewhere?
            // userAgent(loritta.lorittaCluster.getUserAgent())
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
    suspend fun getUserProfileBackgroundUrl(profile: PuddingUserProfile): String {
        val profileSettings = profile.getProfileSettings()
        val activeProfileDesignInternalName = profileSettings.activeProfileDesign
        val activeBackgroundInternalName = profileSettings.activeBackground
        // TODO: Fix default profile design ID
        return getUserProfileBackgroundUrl(profile.id.value.toLong(), profileSettings.id, activeProfileDesignInternalName ?: "defaultDark" /* ProfileDesign.DEFAULT_PROFILE_DESIGN_ID */, activeBackgroundInternalName ?: Background.DEFAULT_BACKGROUND_ID)
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
        val defaultBlueBackground = services.backgrounds.getBackground(Background.DEFAULT_BACKGROUND_ID)!!
        var background = services.backgrounds.getBackground(activeBackgroundInternalName) ?: defaultBlueBackground

        if (background.id == Background.RANDOM_BACKGROUND_ID) {
            // If the user selected a random background, we are going to get all the user's backgrounds and choose a random background from the list
            val allBackgrounds = mutableListOf(defaultBlueBackground)

            allBackgrounds.addAll(
                services.transaction {
                    (BackgroundPayments innerJoin Backgrounds).select {
                        BackgroundPayments.userId eq userId
                    }.map {
                        val data = Background.fromRow(it)
                        PuddingBackground(
                            services,
                            data
                        )
                    }
                }
            )

            background = allBackgrounds.random()
        }

        if (background.id == Background.CUSTOM_BACKGROUND_ID) {
            // Custom background
            val donationValue = services.payments.getActiveMoneyFromDonations(UserId(userId))
            val plan = UserPremiumPlans.getPlanFromValue(donationValue)

            if (plan.customBackground) {
                val dssNamespace = dreamStorageService.getCachedNamespaceOrRetrieve()
                val resultRow = services.transaction {
                    CustomBackgroundSettings.select { CustomBackgroundSettings.settings eq settingsId }
                        .firstOrNull()
                }

                // If the path exists, then the background (probably!) exists
                if (resultRow != null) {
                    val file = resultRow[CustomBackgroundSettings.file]
                    val extension = MediaTypeUtils.convertContentTypeToExtension(resultRow[CustomBackgroundSettings.preferredMediaType])
                    return "${config.services.dreamStorageService.url}/$dssNamespace/${StoragePaths.CustomBackground(userId, file).join()}.$extension"
                }
            }

            // If everything fails, change the background to the default blue background
            // This is required because the current background is "CUSTOM", so Loritta will try getting the default variation of the custom background...
            // but that doesn't exist!
            background = defaultBlueBackground
        }

        val dssNamespace = dreamStorageService.getCachedNamespaceOrRetrieve()
        val variation = background.getVariationForProfileDesign(activeProfileDesignInternalName)
        return getBackgroundUrlWithCropParameters(config.services.dreamStorageService.url, dssNamespace, variation)
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

    suspend fun <T> redisConnection(action: (Jedis) -> (T)) = withContext(Dispatchers.IO) {
        jedisPool.resource.use {
            action.invoke(it)
        }
    }

    suspend fun <T> redisTransaction(action: (Transaction) -> (T)) = redisConnection {
        val t = it.multi()
        try {
            action.invoke(t)
            t.exec()
        } catch (e: Throwable) {
            t.discard()
            throw e
        }
    }
}