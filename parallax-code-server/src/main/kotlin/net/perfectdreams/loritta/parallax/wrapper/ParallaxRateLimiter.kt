package net.perfectdreams.loritta.parallax.wrapper

import kotlinx.coroutines.Job
import mu.KotlinLogging
import org.graalvm.polyglot.Context

class ParallaxRateLimiter(private val jsContext: JSCommandContext, private val context: Context) {
	companion object {
		val logger = KotlinLogging.logger {}
	}

	private var requestCount = 0
	private val commandSpecificTasks = mutableListOf<Job>()

	internal fun reset() {
		requestCount = 0
	}

	fun addAndCheck() {
		if (requestCount == 25)
			throw RuntimeException("Too many requests!")

		if (requestCount % 2 == 1) {
			println("Request is rate limited! requestCount = $requestCount")
			Thread.sleep(2_500)
		}

		requestCount++
	}

	fun cancelAllTasks() {
		commandSpecificTasks.onEach { it.cancel() }
		commandSpecificTasks.clear()
	}
}