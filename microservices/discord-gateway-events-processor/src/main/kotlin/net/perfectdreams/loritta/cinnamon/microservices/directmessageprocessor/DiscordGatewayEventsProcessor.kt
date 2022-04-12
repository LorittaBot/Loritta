package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor

import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils.DirectMessageProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    val services: Pudding
) {
    val rest = RestClient(config.discord.token)
    val starboardModule = StarboardModule(this)

    fun start() {
        val tasks = DirectMessageProcessorTasks(this)
        tasks.start()
    }
}