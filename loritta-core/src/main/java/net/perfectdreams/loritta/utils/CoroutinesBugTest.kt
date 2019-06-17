package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.lang.management.ManagementFactory
import java.util.concurrent.ThreadPoolExecutor
import kotlin.concurrent.thread

class CoroutinesBugTest {
	private val coroutinesLogger = KotlinLogging.logger("coroutinesbug")

	fun start() {
		thread {
			while (true) {
				val async = GlobalScope.async(loritta.coroutineDispatcher) {
					val startNanos = System.nanoTime()
					val start = System.currentTimeMillis()
					coroutinesLogger.info { "Waiting 60s inside coroutine $this... start = $start; startNanos = $startNanos; Total thread count = ${ManagementFactory.getThreadMXBean().threadCount}; Coroutine Executor thread count: ${(loritta.coroutineExecutor as ThreadPoolExecutor).activeCount}" }

					delay(60_000)

					val end = System.currentTimeMillis()
					val endNanos = System.nanoTime()
					coroutinesLogger.info { "Successfully waited 60s inside coroutine $this! end = $end; endNanos = $endNanos; Total thread count = ${ManagementFactory.getThreadMXBean().threadCount}; Coroutine Executor thread count: ${(loritta.coroutineExecutor as ThreadPoolExecutor).activeCount}; Took ${end - start}ms to complete!" }
				}

				runBlocking {
					async.await()
				}
			}
		}
	}
}