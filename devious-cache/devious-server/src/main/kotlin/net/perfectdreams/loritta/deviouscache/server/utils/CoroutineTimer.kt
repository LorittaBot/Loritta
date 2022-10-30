package net.perfectdreams.loritta.deviouscache.server.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

/**
 * Schedules [action] to be executed on [scope] every [period] with a [initialDelay]
 */
fun scheduleCoroutineAtFixedRate(scope: CoroutineScope, period: Duration, initialDelay: Duration = Duration.ZERO, action: RunnableCoroutine): Job {
    return scope.launch {
        delay(initialDelay)

        val mutex = Mutex()

        while (true) {
            launch {
                mutex.withLock {
                    action.run()
                }
            }
            delay(period)
        }
    }
}

fun interface RunnableCoroutine {
    suspend fun run()
}