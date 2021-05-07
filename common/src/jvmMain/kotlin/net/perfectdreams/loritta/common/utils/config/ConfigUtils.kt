package net.perfectdreams.loritta.common.utils.config

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import java.io.File
import java.io.FileNotFoundException
import kotlin.reflect.KClass
import kotlin.system.exitProcess

object ConfigUtils {
    // TODO: This is temporary and should be moved to the config files
    val localesFolder = File("locales")
    private const val DEFAULT_CONFIG_FILE_NAME = "loritta.conf"

    /**
     * The default configuration file name or, if present, the file name in the `loritta.config` property
     */
    val defaultConfigFileName = System.getProperty("loritta.config", DEFAULT_CONFIG_FILE_NAME)

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
    inline fun <reified T> loadAndParseConfig(file: File) = loadAndParseConfigOrNull<T>(file) ?: throw FileNotFoundException("Cannot load config because $file does not exist")

    /**
     * Loads the config file from [path] and parses it with [Hocon], deserialized to a object of type [T]
     *
     * If the file doesn't exist, the returned value will be null. Useful for "load if exists, create file if it doesn't" code paths.
     *
     * @param path the configuration path (Example: `./loritta.conf`)
     * @return the deserialized configuration or null if the file does not exist
     */
    inline fun <reified T> loadAndParseConfigOrNull(path: String): T? = loadAndParseConfigOrNull(File(path))

    /**
     * Loads the config file from [path] and parses it with [Hocon], deserialized to a object of type [T]
     *
     * If the file doesn't exist, the returned value will be null. Useful for "load if exists, create file if it doesn't" code paths.
     *
     * @param path the configuration path (Example: `./loritta.conf`)
     * @return the deserialized configuration or null if the file does not exist
     */
    inline fun <reified T> loadAndParseConfigOrNull(file: File): T? {
        if (!file.exists())
            return null
        val fileConfig = ConfigFactory.parseFile(file)
        return Hocon.decodeFromConfig(fileConfig)
    }

    /**
     * Copies a file within the JAR at the [inputPath] to the [outputFile]
     *
     * @param clazz      a class reference, the resource file should be in the same JAR as this class
     * @param inputPath  where the file is located within the JAR
     * @param outputPath where the file should be copied to
     */
    fun copyFromJar(clazz: KClass<*>, inputPath: String, outputPath: String) = copyFromJar(clazz, inputPath, File(outputPath))

    /**
     * Copies a file within the JAR at the [inputPath] to the [outputFile]
     *
     * @param clazz      a class reference, the resource file should be in the same JAR as this class
     * @param inputPath  where the file is located within the JAR
     * @param outputPath where the file should be copied to
     */
    fun copyFromJar(clazz: KClass<*>, inputPath: String, outputFile: File) {
        val inputStream = clazz::class.java.getResourceAsStream(inputPath)
        outputFile.writeBytes(inputStream.readAllBytes())
    }

    /**
     * Loads the config file from [path] and parses it with [Hocon], deserialized to a object of type [T]
     *
     * If the file doesn't exist, the returned value will be null. Useful for "load if exists, create file if it doesn't" code paths.
     *
     * @param path the configuration path (Example: `./loritta.conf`)
     * @return the deserialized configuration or null if the file does not exist
     */
    inline fun <reified T> loadAndParseConfigOrCopyFromJarAndExit(clazz: KClass<*>, path: String): T = loadAndParseConfigOrNull<T>(path) ?: run {
        copyFromJar(clazz, "/$path", "./$path")

        println("Welcome to Loritta Morenitta! :3")
        println("")
        println("I want to make the world a better place... helping people, making them laugh... I hope I succeed!")
        println("")
        println("Before we start, you need to configure me!")
        println("I created a file named \"loritta.conf\", there you can configure a lot of things and stuff related to me, open it on your favorite text editor and change it!")
        println("")
        println("After configuring the file, run me again!")

        exitProcess(0)
    }
}