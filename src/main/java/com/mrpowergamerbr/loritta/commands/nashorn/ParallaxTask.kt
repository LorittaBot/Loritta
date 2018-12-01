package com.mrpowergamerbr.loritta.commands.nashorn

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxContext
import org.graalvm.polyglot.Context
import java.lang.management.ManagementFactory
import java.util.concurrent.Callable

internal class ParallaxTask(var graalContext: Context, var javaScript: String, var ogContext: CommandContext, var context: ParallaxContext) : Callable<Void> {
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
					println("bytes: " + sunBean.getThreadAllocatedBytes(id))
					autoKill++
					if (sunBean.getThreadAllocatedBytes(id) > 227402240 || autoKill > 600) {
						println("!!! Matando thread")
						running = false
						graalContext.close(true)
					}
					try {
						Thread.sleep(25)
					} catch (e: Exception) {
					}

				}
				return
			}
		}
		t.start()

		val value = graalContext.eval("js", javaScript)
		value.execute(context)

		running = false
		return null
	}
}