package com.mrpowergamerbr.loritta

import com.google.gson.GsonBuilder
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import java.io.File
import java.io.IOException

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
			println("Bem-Vindo(a) a Loritta!")
			println("Como é a sua primeira vez executando ela, nós iremos criar um arquivo chamado \"config.json\", que você deverá configurar a Loritta antes de usar ela!")
			println("")
			println("Após configurar a Loritta, inicie ela novamente!")
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