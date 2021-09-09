package net.perfectdreams.loritta.cinnamon.interaktions.webserver

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.memory.services.MemoryServices
import net.perfectdreams.loritta.cinnamon.common.pudding.services.PuddingServices
import net.perfectdreams.loritta.cinnamon.common.utils.config.ConfigUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.Prometheus
import net.perfectdreams.loritta.cinnamon.interaktions.webserver.utils.config.RootConfig

object LorittaInteraKTionsLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val rootConfig = ConfigUtils.loadAndParseConfigOrCopyFromJarAndExit<RootConfig>(LorittaInteraKTions::class, ConfigUtils.defaultConfigFileName)
        logger.info { "Loaded Loritta's configuration file" }

        Prometheus.register()
        logger.info { "Registered Prometheus Metrics" }

        val languageManager = LanguageManager(
            LorittaInteraKTions::class,
            "en",
            "/languages/"
        )
        languageManager.loadLanguagesAndContexts()

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
            rootConfig.interactionsEndpoint,
            rootConfig.services,
            languageManager,
            services,
            http
        )

        loritta.start()
    }
}