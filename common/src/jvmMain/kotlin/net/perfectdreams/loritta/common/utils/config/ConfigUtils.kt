package net.perfectdreams.loritta.common.utils.config

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File

object ConfigUtils {
    // TODO: This is temporary and should be moved to the config files
    val localesFolder = File("locales")

    /**
     * Loads the config file from [path] and parses it with [Hocon], deserialized to a object of type [T]
     *
     * @param path the configuration path (Example: `./loritta.conf`)
     * @return the deserialized configuration
     */
    inline fun <reified T> loadAndParseConfig(path: String): T = loadAndParseConfig(File(path))

    /**
     * Loads the config file from [path] and parses it with [Hocon], deserialized to a object of type [T]
     *
     * @param file the configuration file (Example: `File("./loritta.conf")`)
     * @return the deserialized configuration
     */
    inline fun <reified T> loadAndParseConfig(file: File): T {
        val fileConfig = ConfigFactory.parseFile(file)
        return Hocon.decodeFromConfig(fileConfig)
    }
}