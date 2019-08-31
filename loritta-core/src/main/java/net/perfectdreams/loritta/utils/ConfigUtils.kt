package net.perfectdreams.loritta.utils

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.utils.Constants
import java.io.File
import java.io.IOException

inline fun <reified T> readConfigurationFromFile(file: File): T {
	try {
		val json = file.readText()
		return Constants.HOCON_MAPPER.readValue<T>(json)
	} catch (e: IOException) {
		e.printStackTrace()
		System.exit(1) // Sair caso der erro
		throw e
	}
}