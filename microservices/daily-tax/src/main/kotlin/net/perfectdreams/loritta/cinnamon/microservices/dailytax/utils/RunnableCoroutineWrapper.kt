package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import kotlinx.coroutines.runBlocking

abstract class RunnableCoroutineWrapper : Runnable {
    override fun run() = runBlocking {
        runCoroutine()
    }

    abstract suspend fun runCoroutine()
}