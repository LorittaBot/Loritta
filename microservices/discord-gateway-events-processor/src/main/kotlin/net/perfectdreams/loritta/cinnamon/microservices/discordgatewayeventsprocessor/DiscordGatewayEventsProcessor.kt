package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import dev.kord.gateway.Event
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
import java.security.SecureRandom
import java.util.concurrent.ConcurrentLinkedQueue

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

    fun start() {
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

    private fun launchEventProcessorJob(shardId: Int, discordEvent: Event) {
        val coroutineName = "Event ${discordEvent::class.simpleName}"
        launchEventJob(coroutineName) {
            try {
                for (module in modules) {
                    val result = module.processEvent(shardId, discordEvent)
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

    private fun launchEventJob(coroutineName: String, block: suspend CoroutineScope.() -> Unit) {
        val start = System.currentTimeMillis()
        val job = GlobalScope.launch(
            CoroutineName(coroutineName),
            block = block
        )

        activeEvents.add(job)

        // Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
        // added to the list, causing leaks.
        // invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
        job.invokeOnCompletion {
            activeEvents.remove(job)

            val diff = System.currentTimeMillis() - start
            if (diff >= 60_000) {
                logger.warn { "Coroutine $job ($coroutineName) took too long to process! ${diff}ms" }
            }
        }
    }
}