package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.GuildCountStats
import org.jetbrains.exposed.sql.insert

class StatsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun insertGuildCountStats(
        guildCount: Long,
        time: Instant
    ) {
        pudding.transaction {
            GuildCountStats.insert {
                it[GuildCountStats.timestamp] = time.toJavaInstant()
                it[GuildCountStats.guildCount] = guildCount
            }
        }
    }
}