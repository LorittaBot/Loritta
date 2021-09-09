package net.perfectdreams.loritta.cinnamon.platform.webserver

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandManager
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.services.Services
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.platform.webserver.utils.config.InteractionsEndpointConfig
import net.perfectdreams.loritta.cinnamon.platform.webserver.webserver.InteractionsServer

class LorittaCinnamonWebServer(
    config: LorittaConfig,
    discordConfig: LorittaDiscordConfig,
    interactionsConfig: DiscordInteractionsConfig,
    interactionsEndpointConfig: InteractionsEndpointConfig,
    servicesConfig: ServicesConfig,
    languageManager: LanguageManager,
    services: Services,
    http: HttpClient
): LorittaCinnamon(
    config,
    discordConfig,
    interactionsConfig,
    servicesConfig,
    languageManager,
    services,
    http
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val interactions = InteractionsServer(
        rest = rest,
        applicationId = discordConfig.applicationId,
        publicKey = interactionsEndpointConfig.publicKey,
    )

    val commandManager = CommandManager(
        this,
        interactions.commandManager
    )

    fun start() {
        languageManager.loadLanguagesAndContexts()

        runBlocking {
            commandManager.register()
        }

        interactions.start()
    }
}