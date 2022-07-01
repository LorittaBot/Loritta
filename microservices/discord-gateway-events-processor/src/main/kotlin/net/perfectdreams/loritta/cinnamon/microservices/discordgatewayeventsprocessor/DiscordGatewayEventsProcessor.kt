package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import dev.kord.gateway.Event
import dev.kord.rest.service.RestClient
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AddFirstToNewChannelsModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.DiscordCacheModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.tables.DiscordGatewayEvents
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.DiscordGatewayEventsProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.QueueDatabase
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentLinkedQueue

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    val services: Pudding,
    val queueDatabase: QueueDatabase,
    val languageManager: LanguageManager
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val rest = RestClient(config.discord.token)
    val starboardModule = StarboardModule(this)
    val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)
    val discordCacheModule = DiscordCacheModule(this)

    val activeEvents = ConcurrentLinkedQueue<Job>()
    val modules = listOf(
        starboardModule,
        addFirstToNewChannelsModule,
        discordCacheModule
    )

    val tasks = DiscordGatewayEventsProcessorTasks(this)

    fun start() {
        runBlocking {
            transaction(queueDatabase.database) {
                SchemaUtils.createMissingTablesAndColumns(
                    DiscordGatewayEvents
                )
            }
        }

        tasks.start()
    }

    fun launchEventProcessorJob(discordEvent: Event) {
        val coroutineName = "Event ${discordEvent::class.simpleName}"
        launchEventJob(coroutineName) {
            try {
                discordCacheModule.processEvent(discordEvent)
                addFirstToNewChannelsModule.processEvent(discordEvent)
                starboardModule.processEvent(discordEvent)
            } catch (e: Throwable) {
                logger.warn(e) { "Something went wrong while trying to process $coroutineName! We are going to ignore..." }
            }
        }
    }

    fun launchEventJob(coroutineName: String, block: suspend CoroutineScope.() -> Unit) {
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