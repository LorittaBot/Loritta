package net.perfectdreams.loritta.cinnamon.microservices.dailytax

import io.ktor.client.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils.DailyTaxWarner
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DailyTax(val config: RootConfig, val services: Pudding, val http: HttpClient) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun start() {
        DailyTaxWarner(this).run()
        // val tasks = DailyTaxTasks(this)
        // tasks.start()
    }
}