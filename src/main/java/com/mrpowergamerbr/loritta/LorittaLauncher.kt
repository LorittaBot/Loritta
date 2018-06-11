package com.mrpowergamerbr.loritta

import com.google.gson.GsonBuilder
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
		val gson = GsonBuilder().setPrettyPrinting().create()
		val file = File(System.getProperty("conf") ?: "./config.json")
		var config: LorittaConfig? = null

		if (file.exists()) {
			val json: String
			try {
				json = file.readText()
				config = gson.fromJson(json, LorittaConfig::class.java)
			} catch (e: IOException) {
				e.printStackTrace()
				System.exit(1) // Sair caso der erro
				return
			}

		} else {
			println("Welcome to Loritta!")
			println("Because this is your first time executing me, I will create a file named \"config.json\", that you will need to configure before using me!")
			println("")
			println("After configuring the file, run me again!")
			try {
				file.writeText(gson.toJson(LorittaConfig()))
			} catch (e: IOException) {
				e.printStackTrace()
			}

			System.exit(1)
			return
		}

		// Iniciar instância da Loritta
		loritta = Loritta(config)
		loritta.start()
	}
}