package net.perfectdreams.loritta.legacy.utils

import com.fasterxml.jackson.module.kotlin.readValue
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.HoconUtils.decodeFromString
import java.io.File
import java.io.IOException

inline fun <reified T> readConfigurationFromFile(file: File): T {
	try {
		val json = file.readText()
		return Constants.HOCON.decodeFromString<T>(json)
	} catch (e: IOException) {
		e.printStackTrace()
		System.exit(1) // Sair caso der erro
		throw e
	}
}