package net.perfectdreams.loritta.morenitta.mines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.MinesSinglePlayerMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.StoredBlackjackRefundTransaction
import net.perfectdreams.loritta.serializable.StoredMinesRefundTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*

class MinesManager(val loritta: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    // This is used to issue refunds when Loritta restarts, if the UUID is different than the current UUID AND the match isn't finished, it means that we should refund the user
    val uniqueId = UUID.randomUUID()

    fun startRefundMinesTask() {
        GlobalScope.launch {
            refundMines()
        }
    }

    private suspend fun refundMines() {
        logger.info { "Checking if there is any mines matches to be refunded..." }

        val (refundedMatches, refundedMatchesNoBets) = loritta.transaction {
            var refundedMatches = 0
            var refundedMatchesNoBets = 0

            val expiredMatches = MinesSinglePlayerMatches.selectAll()
                .where {
                    MinesSinglePlayerMatches.finishedAt.isNull() and (MinesSinglePlayerMatches.lorittaClusterId eq loritta.clusterId) and (MinesSinglePlayerMatches.minesManagerUniqueId neq this@MinesManager.uniqueId)
                }
                .toList()

            for (match in expiredMatches) {
                val now = Instant.now()

                val payoutValue = match[MinesSinglePlayerMatches.bet]
                logger.info { "Refunding match ${match[MinesSinglePlayerMatches.id]} because the stored UUID is ${match[MinesSinglePlayerMatches.minesManagerUniqueId]} while the current active UUID is ${this@MinesManager.uniqueId} - Refund should be $payoutValue" }

                if (payoutValue != null) {
                    MinesSinglePlayerMatches.update({ MinesSinglePlayerMatches.id eq match[MinesSinglePlayerMatches.id] }) {
                        it[MinesSinglePlayerMatches.finishedAt] = now
                        it[MinesSinglePlayerMatches.payout] = payoutValue
                        it[MinesSinglePlayerMatches.refunded] = true
                    }

                    Profiles.update({ Profiles.id eq match[MinesSinglePlayerMatches.user] }) {
                        it[Profiles.money] = Profiles.money + payoutValue
                    }

                    SimpleSonhosTransactionsLogUtils.insert(
                        match[MinesSinglePlayerMatches.user],
                        now,
                        TransactionType.MINES,
                        payoutValue, // We pay as is
                        StoredMinesRefundTransaction(match[MinesSinglePlayerMatches.id].value)
                    )

                    refundedMatches++
                } else {
                    MinesSinglePlayerMatches.update({ MinesSinglePlayerMatches.id eq match[MinesSinglePlayerMatches.id] }) {
                        it[MinesSinglePlayerMatches.finishedAt] = now
                    }

                    refundedMatchesNoBets++
                }
            }

            Pair(refundedMatches, refundedMatchesNoBets)
        }

        logger.info { "Refunded $refundedMatches mines and $refundedMatchesNoBets mines without bets!" }
    }
}