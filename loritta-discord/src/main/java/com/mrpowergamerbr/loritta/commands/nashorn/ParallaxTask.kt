package com.mrpowergamerbr.loritta.commands.nashorn

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxContext
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.graalvm.polyglot.Context
import java.lang.management.ManagementFactory
import java.util.concurrent.Callable

internal class ParallaxTask(var graalContext: Context, var javaScript: String, var ogContext: CommandContext, var context: ParallaxContext) : Callable<Void> {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var running = true
	var autoKill = 0

	init {
		running = true
	}

	@Throws(Exception::class)
	override fun call(): Void? {
		val sunBean = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
		val id = Thread.currentThread().id
		val currentThread = Thread.currentThread()

		val t = object : Thread() {
			override fun run() {
				while (running) {
					logger.info("${currentThread.name} - bytes: ${sunBean.getThreadAllocatedBytes(id)}")

					autoKill++
					val allocatedBytes = sunBean.getThreadAllocatedBytes(id)

					if (allocatedBytes > 227402240 || autoKill > 600) {
						logger.info("Killing thread ${currentThread.name}, $allocatedBytes allocated bytes, autoKill = $autoKill")
						running = false
					}

					if (!running)
						graalContext.close(true)

					try {
						Thread.sleep(25)
					} catch (e: Exception) {
					}
				}
				return
			}
		}
		t.start()

		try {
			logger.info("Evaluating (GraalJS) @ ${ogContext.guild.idLong} = $javaScript")

			val value = graalContext.eval("js", javaScript)
			value.execute(context)

			running = false
		} catch (t: Throwable) {
			// Cancele primeiro a task
			running = false
			graalContext.close(true)

			GlobalScope.launch(loritta.coroutineDispatcher) {
				ParallaxUtils.sendThrowableToChannel(
						t,
						ogContext.event.channel
				)
			}
		}

		return null
	}
}