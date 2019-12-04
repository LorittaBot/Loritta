package net.perfectdreams.loritta.parallax.wrapper

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.parallax.ParallaxServer
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import org.graalvm.polyglot.proxy.ProxyExecutable

class ParallaxRateLimiter(private val context: Context) {
	companion object {
		val logger = KotlinLogging.logger {}
	}

	private var requestCount = 0
	private val commandSpecificTasks = mutableListOf<Job>()

	fun addAndCheck() {
		if (requestCount == 25)
			throw RuntimeException("Too many requests!")

		if (requestCount % 4 == 2) {
			println("Request is rate limited! requestCount = $requestCount")
			Thread.sleep(2_500)
		}

		requestCount++
	}

	fun wrapPromise(promise: suspend () -> (Any?)): Value? {
		val global: Value = context.getBindings("js")
		val promiseConstructor = global.getMember("Promise")
		return promiseConstructor.newInstance(ProxyExecutable { arguments: Array<Value> ->
			val resolve = arguments[0]
			val reject = arguments[1]

			commandSpecificTasks.add(
					GlobalScope.launch(ParallaxServer.coroutineDispatchers) {
						try {
							val result = promise.invoke()
							resolve.execute(result)
						} catch (e: Throwable) {
							logger.error(e) { "Oof!" }
							reject.execute(e)
						}
					}
			)

			null
		})
	}

	fun cancelAllTasks() {
		commandSpecificTasks.onEach { it.cancel() }
		commandSpecificTasks.clear()
	}
}