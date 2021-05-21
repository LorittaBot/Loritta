package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralDiscordInstanceConfig
import com.mrpowergamerbr.loritta.utils.config.GeneralInstanceConfig
import net.perfectdreams.loritta.utils.HoconUtils.decodeFromString
import org.junit.jupiter.api.Test

class CheckDefaultConfigsTest {
	private fun loadFromJar(inputPath: String): String {
		val inputStream = LorittaLauncher::class.java.getResourceAsStream(inputPath)
		return inputStream.bufferedReader(Charsets.UTF_8).readText()
	}

	@Test
	fun `check general config`() {
		val configurationFile = loadFromJar("/loritta.conf")
		Constants.HOCON.decodeFromString<GeneralConfig>(configurationFile)
	}

	@Test
	fun `check general instance config`() {
		val configurationFile = loadFromJar("/loritta.instance.conf")
		Constants.HOCON.decodeFromString<GeneralInstanceConfig>(configurationFile)
	}

	@Test
	fun `check discord config`() {
		val configurationFile = loadFromJar("/discord.conf")
		Constants.HOCON.decodeFromString<GeneralDiscordConfig>(configurationFile)
	}

	@Test
	fun `check discord instance config`() {
		val configurationFile = loadFromJar("/discord.instance.conf")
		Constants.HOCON.decodeFromString<GeneralDiscordInstanceConfig>(configurationFile)
	}
}