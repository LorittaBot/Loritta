package com.mrpowergamerbr.loritta.commands.nashorn

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.lang.management.ManagementFactory
import java.util.concurrent.Callable
import javax.script.Invocable
import javax.script.ScriptEngine

internal class NashornTask(var engine: ScriptEngine, var javaScript: String, var ogContext: CommandContext, var context: NashornContext) : Callable<Void> {
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
		try {
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

						// Workaround, não se deve usar Thread.stop()!
						if (!running)
							currentThread.stop() // stop now!

						try {
							Thread.sleep(25)
						} catch (e: Exception) {
						}

					}
					return
				}
			}

			t.start()
			logger.info("Evaluating (Nashorn) @ ${ogContext.guild.idLong} = $javaScript")

			val invocable = engine as Invocable
			engine.eval(javaScript)
			invocable.invokeFunction("nashornCommand", context)

			running = false
		} catch (t: Throwable) {
			// Cancele primeiro a task
			running = false

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