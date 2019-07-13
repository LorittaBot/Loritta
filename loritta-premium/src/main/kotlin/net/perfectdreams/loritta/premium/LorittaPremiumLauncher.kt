package net.perfectdreams.loritta.premium

import com.fasterxml.jackson.module.kotlin.readValue
import net.perfectdreams.loritta.premium.utils.Constants
import java.io.File

object LorittaPremiumLauncher {
	@JvmStatic
	fun main(args: Array<String>) {
		LorittaPremium(Constants.HOCON_MAPPER.readValue(File("premium.conf"))).start()
	}
}