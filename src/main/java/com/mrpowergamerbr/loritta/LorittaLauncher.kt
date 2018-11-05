package com.mrpowergamerbr.loritta

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import java.io.File
import java.io.IOException

/**
 * Loritta's Launcher
 *
 * @author MrPowerGamerBR
 */
object LorittaLauncher {
	// STATIC MAGIC(tm)
	lateinit var loritta: Loritta

	@JvmStatic
	fun main(args: Array<String>) {
		val doNotStart = File("do_not_start").exists()
		if (doNotStart) {
			while (true) {
				System.out.println("Falha de segurança detectada!")
				Thread.sleep(120000)
			}
		}

		// https://bugs.openjdk.java.net/browse/JDK-7016595
		// Nós precisamos ativar o PATCH manualmente
		WebsiteUtils.allowMethods("PATCH")

		val file = File(System.getProperty("conf") ?: "./config.yml")
		var config: LorittaConfig? = null

		if (file.exists()) {
			val json: String
			try {
				json = file.readText()
				config = Constants.MAPPER.readValue(json, LorittaConfig::class.java)
			} catch (e: IOException) {
				e.printStackTrace()
				System.exit(1) // Sair caso der erro
				return
			}

		} else {
			println("Welcome to Loritta!")
			println("Because this is your first time executing me, I will create a file named \"config.yml\", that you will need to configure before using me!")
			println("")
			println("After configuring the file, run me again!")
			System.exit(1)
			return
		}

		// Iniciar instância da Loritta
		loritta = Loritta(config)
		loritta.start()
	}
}