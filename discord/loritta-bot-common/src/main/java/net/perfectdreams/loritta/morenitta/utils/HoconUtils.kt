package net.perfectdreams.loritta.morenitta.utils

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File

object HoconUtils {
    inline fun <reified T> Hocon.decodeFromFile(file: File): T = decodeFromConfig(ConfigFactory.parseFile(file).resolve())
    inline fun <reified T> Hocon.decodeFromString(string: String): T = decodeFromConfig(ConfigFactory.parseString(string).resolve())
}