package net.perfectdreams.loritta.watchdog

import com.fasterxml.jackson.module.kotlin.readValue
import net.perfectdreams.loritta.watchdog.utils.Constants
import java.io.File

object WatchdogBotLauncher {
	@JvmStatic
	fun main(args: Array<String>) {
		WatchdogBot(Constants.HOCON_MAPPER.readValue(File("watchdog.conf"))).start()
	}
}