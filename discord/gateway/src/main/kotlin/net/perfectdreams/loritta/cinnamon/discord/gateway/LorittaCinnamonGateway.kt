package net.perfectdreams.loritta.cinnamon.discord.gateway

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.start
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.platforms.kord.installDiscordInteraKTions
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionsManager
import net.perfectdreams.loritta.cinnamon.discord.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class LorittaCinnamonGateway(
    config: LorittaConfig,
    discordConfig: LorittaDiscordConfig,
    interactionsConfig: DiscordInteractionsConfig,
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

    private val gateway = DefaultGateway()

    private val interactionsManager = InteractionsManager(
        this,
        CommandManager()
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

            gateway.installDiscordInteraKTions(
                Snowflake(discordConfig.applicationId),
                rest,
                interactionsManager.interactionsRegistry.interaKTionsManager
            )

            gateway.start(discordConfig.token)
        }
    }
}