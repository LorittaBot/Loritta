package net.perfectdreams.loritta.platform.kord

import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import mu.KotlinLogging
import net.perfectdreams.loritta.common.memory.services.MemoryServices
import net.perfectdreams.loritta.common.pudding.services.PuddingServices
import net.perfectdreams.loritta.platform.kord.utils.config.RootConfig
import java.io.File

object LorittaKordLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = loadConfig()
        logger.info { "Loaded Loritta's configuration file" }

        val http = HttpClient {
            expectSuccess = false
        }

        val services = when (rootConfig.services.lorittaData.type) {
            "PUDDING" -> {
                val puddingConfig = rootConfig.services.lorittaData.pudding ?: throw UnsupportedOperationException("Loritta Data Type is Pudding, but config is not present!")

                PuddingServices(
                    puddingConfig.url.removeSuffix("/"), // Remove trailing slash
                    puddingConfig.authorization,
                    http
                )
            }
            "MEMORY" -> {
                MemoryServices()
            }
            else -> throw UnsupportedOperationException("Unsupported Loritta Data Type: ${rootConfig.services.lorittaData.type}")
        }

        val loritta = LorittaKord(rootConfig.loritta, rootConfig.discord, services)
        loritta.start()
    }

    private fun loadConfig(): RootConfig {
        val fileConfig = ConfigFactory.parseFile(File("./loritta.conf"))
        return Hocon.decodeFromConfig(fileConfig)
    }
}