package net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.utils.CorreiosTrackerTasks
import net.perfectdreams.loritta.cinnamon.microservices.correiospackagetracker.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class CorreiosPackageTracker(
    val config: RootConfig,
    val services: Pudding,
    val languageManager: LanguageManager
) {
    val correiosClient = CorreiosClient()

    fun start() {
        val tasks = CorreiosTrackerTasks(this)
        tasks.start()
    }
}