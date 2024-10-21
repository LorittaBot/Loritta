package net.perfectdreams.loritta.morenitta.analytics

import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import net.perfectdreams.loritta.cinnamon.pudding.tables.TotalSonhosStats
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.sum
import java.time.Instant

/**
 * A class that periodically stores stats to the database, "poor man's Prometheus"
 */
class MagicStats(val loritta: LorittaBot) : RunnableCoroutine {
    override suspend fun run() {
        if (loritta.isMainInstance) {
            loritta.transaction {
                val sumField = Profiles.money.sum()
                val totalSonhos = Profiles.select(sumField)
                    .where {
                        Profiles.money greater 0
                    }
                    .first()[sumField] ?: 0

                val totalSonhosOfBannedUsers = Profiles.select(sumField)
                    .where {
                        Profiles.money greater 0 and (Profiles.id inSubQuery UsersService.validBannedUsersList(System.currentTimeMillis()))
                    }
                    .first()[sumField] ?: 0

                val sonhosBrokerSumField = TickerPrices.value.sum()
                val totalSonhosBroker = BoughtStocks.innerJoin(TickerPrices, { BoughtStocks.ticker }, { TickerPrices.ticker })
                    .select(sonhosBrokerSumField)
                    .first()[sonhosBrokerSumField] ?: 0

                TotalSonhosStats.insert {
                    it[TotalSonhosStats.timestamp] = Instant.now()
                    it[TotalSonhosStats.totalSonhos] = totalSonhos
                    it[TotalSonhosStats.totalSonhosOfBannedUsers] = totalSonhosOfBannedUsers
                    it[TotalSonhosStats.totalSonhosBroker] = totalSonhosBroker
                }
            }
        }
    }
}