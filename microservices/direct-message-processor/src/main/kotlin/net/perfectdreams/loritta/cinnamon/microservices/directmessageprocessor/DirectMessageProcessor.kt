package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor

import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils.DirectMessageProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DirectMessageProcessor(
    val config: RootConfig,
    val languageManager: LanguageManager,
    val services: Pudding
) {
    val rest = RestClient(config.discord.token)

    fun start() {
        val tasks = DirectMessageProcessorTasks(this)
        tasks.start()
    }
}