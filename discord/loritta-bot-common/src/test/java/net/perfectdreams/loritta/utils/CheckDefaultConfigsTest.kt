package net.perfectdreams.loritta.legacy.utils

import net.perfectdreams.loritta.legacy.LorittaLauncher
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.config.GeneralConfig
import net.perfectdreams.loritta.legacy.utils.config.GeneralDiscordConfig
import net.perfectdreams.loritta.legacy.utils.config.GeneralDiscordInstanceConfig
import net.perfectdreams.loritta.legacy.utils.config.GeneralInstanceConfig
import net.perfectdreams.loritta.legacy.utils.HoconUtils.decodeFromString
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