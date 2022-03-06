package net.perfectdreams.loritta.cinnamon.microservices.dailytax

import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils.DailyTaxTasks
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DailyTax(
    val config: RootConfig,
    val services: Pudding,
    val languageManager: LanguageManager
) {
    val rest = RestClient(config.discord.token)

    fun start() {
        val tasks = DailyTaxTasks(this)
        tasks.start()
    }
}