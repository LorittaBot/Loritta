package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils.DirectMessageProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.discord.LorittaDiscordStuff
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DirectMessageProcessor(
    val config: RootConfig,
    val languageManager: LanguageManager,
    services: Pudding
) : LorittaDiscordStuff(config.discord, services) {
    fun start() {
        val tasks = DirectMessageProcessorTasks(this)
        tasks.start()
    }
}