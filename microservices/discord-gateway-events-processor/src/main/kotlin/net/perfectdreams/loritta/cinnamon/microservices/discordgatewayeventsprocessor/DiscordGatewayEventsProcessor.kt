package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AddFirstToNewChannelsModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.DiscordGatewayEventsProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    val services: Pudding,
    val languageManager: LanguageManager
) {
    val rest = RestClient(config.discord.token)
    val starboardModule = StarboardModule(this)
    val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)

    fun start() {
        val tasks = DiscordGatewayEventsProcessorTasks(this)
        tasks.start()
    }
}