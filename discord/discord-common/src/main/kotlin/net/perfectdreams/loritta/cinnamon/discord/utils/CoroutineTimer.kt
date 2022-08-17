package net.perfectdreams.loritta.cinnamon.discord.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.time.Duration

/**
 * Schedules [action] to be executed on [scope] every [period] with a [initialDelay]
 */
fun scheduleCoroutineAtFixedRate(scope: CoroutineScope, period: Duration, initialDelay: Duration = Duration.ZERO, action: RunnableCoroutine) {
    val channel = Channel<Unit>(Channel.UNLIMITED)

    scope.launch {
        try {
            coroutineScope {
                val jobs = listOf(
                    async {
                        delay(initialDelay)

                        while (isActive) {
                            channel.send(Unit)
                            delay(period)
                        }
                    },
                    async {
                        for (notification in channel) {
                            action.run()
                        }
                    }
                )

                jobs.awaitAll()
            }
        } finally {
            channel.cancel()
        }
    }
}

interface RunnableCoroutine {
    suspend fun run()
}