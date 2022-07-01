package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import dev.kord.gateway.Event
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.ConcurrentLinkedQueue

abstract class ProcessDiscordEventsModule {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val clazzName = this::class.simpleName
    val activeEvents = ConcurrentLinkedQueue<Job>()

    fun launchEventProcessorJob(discordEvent: Event) {
        val coroutineName = "Event ${discordEvent::class.simpleName} for $clazzName"
        launchEventJob(coroutineName) {
            try {
                processEvent(discordEvent)
            } catch (e: Throwable) {
                logger.warn(e) { "Something went wrong while trying to process $coroutineName! We are going to ignore..." }
            }
        }
    }

    abstract suspend fun processEvent(event: Event)

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
                logger.warn { "Coroutine $job ($clazzName) took too long to process! ${diff}ms" }
            }
        }
    }
}