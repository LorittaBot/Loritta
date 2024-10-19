package net.perfectdreams.loritta.morenitta.analytics.stats

import kotlinx.datetime.Clock
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.TotalSonhosStats
import org.jetbrains.exposed.sql.insert
import java.time.Instant

class DatabaseStatsSender(private val pudding: Pudding) : StatsSender {
    override suspend fun send(guildCount: Long) {
        pudding.transaction {
            pudding.stats.insertGuildCountStats(
                guildCount,
                Clock.System.now()
            )
        }
    }
}