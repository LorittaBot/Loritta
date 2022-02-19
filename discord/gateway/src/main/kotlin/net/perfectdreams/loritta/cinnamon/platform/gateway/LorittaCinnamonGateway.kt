package net.perfectdreams.loritta.cinnamon.platform.gateway

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.start
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.platforms.kord.installDiscordInteraKTions
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandManager
import net.perfectdreams.loritta.cinnamon.platform.utils.CinnamonMessageQueueListener
import net.perfectdreams.loritta.cinnamon.platform.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

@OptIn(KordPreview::class)
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

    private val commandManager = CommandManager(
        this,
        net.perfectdreams.discordinteraktions.common.commands.CommandManager()
    )

    fun start() {
        runBlocking {
            val tableNames = servicesConfig.pudding.tablesAllowedToBeUpdated
            services.createMissingTablesAndColumns {
                if (tableNames == null)
                    true
                else it in tableNames
            }
            services.messageQueueListener = CinnamonMessageQueueListener(this@LorittaCinnamonGateway)
            services.startPuddingTasks()

            commandManager.register()

            gateway.installDiscordInteraKTions(
                Snowflake(discordConfig.applicationId),
                rest,
                commandManager.commandManager.interaKTionsManager
            )

            gateway.start(discordConfig.token)
        }
    }
}