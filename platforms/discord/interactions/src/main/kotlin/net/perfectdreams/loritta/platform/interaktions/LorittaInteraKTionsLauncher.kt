package net.perfectdreams.loritta.platform.interaktions

import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.memory.services.MemoryServices
import net.perfectdreams.loritta.common.pudding.services.PuddingServices
import net.perfectdreams.loritta.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.platform.interaktions.emotes.DiscordEmoteManager
import net.perfectdreams.loritta.platform.interaktions.utils.config.RootConfig
import net.perfectdreams.loritta.platform.interaktions.utils.metrics.Prometheus
import java.io.File

object LorittaInteraKTionsLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfig<RootConfig>("./loritta.conf")
        logger.info { "Loaded Loritta's configuration file" }

        val emotes = loadEmotes()
        logger.info { "Loaded ${emotes.size} emotes" }

        Prometheus.register()
        logger.info { "Registered Prometheus Metrics" }

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

        logger.info { "Using ${rootConfig.services.lorittaData.type} services $services" }

        val loritta = LorittaInteraKTions(
            rootConfig.loritta,
            rootConfig.discord,
            rootConfig.interactions,
            services,
            rootConfig.services.gabrielaImageServer,
            Emotes(DiscordEmoteManager(emotes)),
            http
        )

        loritta.start()
    }

    private fun loadEmotes(): Map<String, String> {
        val emotesFile = File("./emotes.conf")
        val fileConfig = ConfigFactory.parseFile(emotesFile)

        return fileConfig.entrySet().map {
            it.key to it.value.unwrapped() as String
        }.toMap()
    }
}