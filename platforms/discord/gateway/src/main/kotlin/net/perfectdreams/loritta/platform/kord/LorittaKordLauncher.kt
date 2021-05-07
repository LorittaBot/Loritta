package net.perfectdreams.loritta.platform.kord

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.common.memory.services.MemoryServices
import net.perfectdreams.loritta.common.pudding.services.PuddingServices
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.platform.kord.utils.config.RootConfig

object LorittaKordLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaKord::class, ConfigUtils.defaultConfigFileName)
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
}