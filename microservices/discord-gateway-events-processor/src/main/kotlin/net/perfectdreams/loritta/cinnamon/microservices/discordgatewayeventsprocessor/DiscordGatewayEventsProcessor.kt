package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import dev.kord.gateway.Event
import dev.kord.gateway.GuildCreate
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy.GatewayProxy
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy.GatewayProxyEvent
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AFKModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AddFirstToNewChannelsModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.BomDiaECiaModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.DebugGatewayModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.DiscordCacheModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.InviteBlockerModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.ModuleResult
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.BomDiaECia
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.DiscordGatewayEventsProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.KordDiscordEventUtils
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.platform.LorittaDiscordStuff
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordEmojis
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuilds
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordRoles
import org.jetbrains.exposed.sql.SchemaUtils
import java.io.File
import java.security.SecureRandom
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    services: Pudding,
    val languageManager: LanguageManager,
    val replicaId: Int
) : LorittaDiscordStuff(config.discord, services) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val starboardModule = StarboardModule(this)
    private val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)
    private val discordCacheModule = DiscordCacheModule(this)
    private val bomDiaECiaModule = BomDiaECiaModule(this)
    private val debugGatewayModule = DebugGatewayModule(this)
    private val inviteBlockerModule = InviteBlockerModule(this)
    private val afkModule = AFKModule(this)

    // This is executed sequentially!
    val modules = listOf(
        discordCacheModule,
        inviteBlockerModule,
        afkModule,
        addFirstToNewChannelsModule,
        starboardModule,
        debugGatewayModule
    )

    val bomDiaECia = BomDiaECia(this)
    val random = SecureRandom()
    val activeEvents = ConcurrentLinkedQueue<Job>()

    private val onMessageReceived: (GatewayProxyEvent) -> (Unit) = {
        val (eventType, discordEvent) = parseEvent(it)

        // We will call a method that doesn't reference the "discordEventAsJsonObject" nor the "it" object, this makes it veeeery clear to the JVM that yes, you can GC the "discordEventAsJsonObject" and "it" objects
        // (Will it really GC the object? idk, but I hope it will)
        launchEventProcessorJob(it.shardId, eventType, discordEvent)
    }

    val gatewayProxies = config.gatewayProxies.filter { it.replicaId == replicaId }.map {
        GatewayProxy(it.url, it.authorizationToken, onMessageReceived)
    }

    // This needs to be initialized AFTER the gatewayProxies above
    val tasks = DiscordGatewayEventsProcessorTasks(this)

    @OptIn(ExperimentalTime::class)
    fun start() {
        if (true) {
            runBlocking {
                services.transaction {
                    SchemaUtils.createMissingTablesAndColumns(
                        DiscordGuilds,
                        DiscordChannels,
                        DiscordRoles,
                        DiscordEmojis
                    )
                }

                val gc = GuildCreate(
                    Json.decodeFromString(File("guild_create.json").readText()),
                    0
                )

                measureTimedValue {
                    val j = (0..100).map {
                        discordCacheModule.processEvent(
                            0,
                            gc
                        )
                    }
                }.let { println("Took ${it.duration}") }
            }
            return
        }

        gatewayProxies.forEachIndexed { index, gatewayProxy ->
            logger.info { "Starting Gateway Proxy $index (${gatewayProxy.url})" }
            gatewayProxy.start()
        }

        tasks.start()

        // bomDiaECia.startBomDiaECiaTask()
    }

    private fun parseEvent(discordGatewayEvent: GatewayProxyEvent): Pair<String, Event?> {
        val discordEventAsJsonObject = discordGatewayEvent.event
        val eventType = discordEventAsJsonObject["t"]?.jsonPrimitive?.content ?: "UNKNOWN"
        val discordEvent = KordDiscordEventUtils.parseEventFromJsonObject(discordEventAsJsonObject)

        return Pair(eventType, discordEvent)
    }

    private fun launchEventProcessorJob(shardId: Int, type: String, discordEvent: Event?) {
        if (discordEvent != null)
            launchEventProcessorJob(shardId, discordEvent)
        else
            logger.warn { "Unknown Discord event received $type! We are going to ignore the event... kthxbye!" }
    }

    @OptIn(ExperimentalTime::class)
    private fun launchEventProcessorJob(shardId: Int, discordEvent: Event) {
        val coroutineName = "Event ${discordEvent::class.simpleName}"
        launchEventJob(coroutineName) {
            try {
                for (module in modules) {
                    val (result, duration) = measureTimedValue { module.processEvent(shardId, discordEvent) }
                    it[module::class] = duration

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
    }

    private fun launchEventJob(coroutineName: String, block: suspend CoroutineScope.(MutableMap<KClass<*>, Duration>) -> Unit) {
        val start = System.currentTimeMillis()
        val durations = mutableMapOf<KClass<*>, Duration>()

        val job = GlobalScope.launch(
            CoroutineName(coroutineName),
            block = {
                block.invoke(this, durations)
            }
        )

        activeEvents.add(job)

        // Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
        // added to the list, causing leaks.
        // invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
        job.invokeOnCompletion {
            activeEvents.remove(job)

            val diff = System.currentTimeMillis() - start
            if (diff >= 60_000) {
                logger.warn { "Coroutine $job ($coroutineName) took too long to process! ${diff}ms - Module Durations: $durations" }
            }
        }
    }
}