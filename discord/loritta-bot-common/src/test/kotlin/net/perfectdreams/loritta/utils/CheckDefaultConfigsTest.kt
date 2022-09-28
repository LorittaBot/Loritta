package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.LorittaLauncher
import net.perfectdreams.loritta.morenitta.utils.HoconUtils.decodeFromString
import net.perfectdreams.loritta.morenitta.utils.config.BaseConfig
import org.junit.jupiter.api.Test

class CheckDefaultConfigsTest {
	private fun loadFromJar(inputPath: String): String {
		val inputStream = LorittaLauncher::class.java.getResourceAsStream(inputPath)
		return inputStream.bufferedReader(Charsets.UTF_8).readText()
	}

	@Test
	fun `check config`() {
		val configurationFile = loadFromJar("/loritta.conf")
		Constants.HOCON.decodeFromString<BaseConfig>(configurationFile)
	}
}