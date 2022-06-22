package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import dev.kord.rest.service.RestClient
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AddFirstToNewChannelsModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.DiscordCacheModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.tables.DiscordGatewayEvents
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.DiscordGatewayEventsProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.QueueDatabase
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    val services: Pudding,
    val servicesWithHttpRequests: Pudding,
    val queueDatabase: QueueDatabase,
    val languageManager: LanguageManager
) {
    val rest = RestClient(config.discord.token)
    val starboardModule = StarboardModule(this)
    val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)
    val discordCacheModule = DiscordCacheModule(this)

    val modules = listOf(
        starboardModule,
        addFirstToNewChannelsModule,
        discordCacheModule
    )

    val tasks = DiscordGatewayEventsProcessorTasks(this)

    fun start() {
        runBlocking {
            transaction(queueDatabase.database) {
                SchemaUtils.createMissingTablesAndColumns(
                    DiscordGatewayEvents
                )
            }
        }

        tasks.start()
    }
}