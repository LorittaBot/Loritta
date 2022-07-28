package net.perfectdreams.loritta.cinnamon.discord.webserver

import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionsManager
import net.perfectdreams.loritta.cinnamon.discord.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.InteractionsEndpointConfig
import net.perfectdreams.loritta.cinnamon.discord.webserver.webserver.InteractionsServer
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class LorittaCinnamonWebServer(
    config: LorittaConfig,
    discordConfig: LorittaDiscordConfig,
    interactionsConfig: DiscordInteractionsConfig,
    interactionsEndpointConfig: InteractionsEndpointConfig,
    servicesConfig: ServicesConfig,
    languageManager: LanguageManager,
    services: Pudding,
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

    val interactionsManager = InteractionsManager(
        this,
        interactions.commandManager
    )

    override fun getCommandCount() = interactionsManager.interaKTionsManager.applicationCommandsExecutors.size

    fun start() {
        runBlocking {
            val tableNames = servicesConfig.pudding.tablesAllowedToBeUpdated
            services.createMissingTablesAndColumns {
                if (tableNames == null)
                    true
                else it in tableNames
            }
            services.startPuddingTasks()

            interactionsManager.register()
        }

        interactions.start()
    }
}