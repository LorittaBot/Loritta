package com.mrpowergamerbr.loritta.listeners.nashorn

import com.sun.management.ThreadMXBean
import java.lang.management.ManagementFactory
import java.util.concurrent.Callable
import javax.script.Invocable
import javax.script.ScriptEngine

internal class NashornEventTask(var engine: ScriptEngine, var javaScript: String, var call: String, vararg objects: Any) : Callable<Void> {
	var objects: Array<out Any>
	var running = true
	var autoKill = 0

	init {
		this.objects = objects
		running = true
	}

	@Throws(Exception::class)
	override fun call(): Void? {
		val sunBean = ManagementFactory.getThreadMXBean() as ThreadMXBean
		val id = Thread.currentThread().id
		val currentThread = Thread.currentThread()
		try {
			val t = object : Thread() {
				override fun run() {
					while (running) {
						autoKill++
						if (sunBean.getThreadAllocatedBytes(id) > 227402240 || autoKill > 600) {
							running = false
							currentThread.stop() // stop now!
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
			val invocable = engine as Invocable
			engine.eval(javaScript)
			invocable.invokeFunction(call, *objects)
		} catch (e: Exception) {
		}

		running = false
		return null
	}
}