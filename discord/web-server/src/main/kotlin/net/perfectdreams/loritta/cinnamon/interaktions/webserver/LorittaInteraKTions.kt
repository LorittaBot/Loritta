package net.perfectdreams.loritta.cinnamon.interaktions.webserver

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.commands.CommandManager
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.services.Services
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.interaktions.webserver.utils.config.InteractionsEndpointConfig
import net.perfectdreams.loritta.cinnamon.interaktions.webserver.webserver.InteractionsServer

class LorittaInteraKTions(
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