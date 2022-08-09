package net.perfectdreams.loritta.cinnamon.discord

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.request.StackTraceRecoveringKtorRequestHandler
import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.DiscordInteraKTions
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.gateway.modules.*
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.config.CinnamonConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.discord.utils.ecb.ECBManager
import net.perfectdreams.loritta.cinnamon.discord.utils.falatron.FalatronModelsManager
import net.perfectdreams.loritta.cinnamon.discord.voice.LorittaVoiceConnectionManager
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionsManager
import net.perfectdreams.loritta.cinnamon.discord.utils.CinnamonTasks
import net.perfectdreams.loritta.cinnamon.discord.utils.EventAnalyticsTask
import net.perfectdreams.loritta.cinnamon.discord.utils.falatron.Falatron
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.PrometheusPushClient
import net.perfectdreams.loritta.cinnamon.discord.utils.soundboard.Soundboard
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.DiscordGatewayEventsProcessorMetrics
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import net.perfectdreams.loritta.cinnamon.pudding.utils.LorittaNotificationListener
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import java.security.SecureRandom
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

/**
 * Represents a Loritta Morenitta (Cinnamon) implementation.
 */
class LorittaCinnamon(
    val gatewayManager: LorittaDiscordGatewayManager,
    val config: CinnamonConfig,

    val languageManager: LanguageManager,
    services: Pudding,
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

    val mojangApi = MinecraftMojangAPI()
    val correiosClient = CorreiosClient()
    val randomRoleplayPicturesClient = RandomRoleplayPicturesClient(config.services.randomRoleplayPictures.url)
    val falatronModelsManager = FalatronModelsManager().also {
        it.startUpdater()
    }
    val ecbManager = ECBManager()
    val falatron = Falatron(config.falatron.url, config.falatron.key)
    val soundboard = Soundboard()

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

    val notificationListener = LorittaNotificationListener(services)
        .apply {
            this.start()
        }

    val analyticHandlers = mutableListOf<EventAnalyticsTask.AnalyticHandler>()
    val cinnamonTasks = CinnamonTasks(this)

    fun start() {
        runBlocking {
            val tableNames = config.services.pudding.tablesAllowedToBeUpdated
            services.createMissingTablesAndColumns {
                if (tableNames == null)
                    true
                else it in tableNames
            }
            services.startPuddingTasks()

            interactionsManager.register()

            cinnamonTasks.start()

            // On every gateway instance present on our gateway manager, collect and process events
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
        }
    }

    private fun launchEventJob(coroutineName: String, durations: Map<KClass<*>, Duration>, block: suspend CoroutineScope.() -> Unit) {
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
}
