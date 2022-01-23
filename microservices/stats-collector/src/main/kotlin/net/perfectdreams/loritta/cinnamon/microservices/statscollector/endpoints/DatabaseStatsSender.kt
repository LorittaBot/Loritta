package net.perfectdreams.loritta.cinnamon.microservices.statscollector.endpoints

import kotlinx.datetime.Clock
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class DatabaseStatsSender(private val pudding: Pudding) : StatsSender {
    override suspend fun send(guildCount: Long) {
        pudding.stats.insertGuildCountStats(
            guildCount,
            Clock.System.now()
        )
    }
}