package net.perfectdreams.loritta.morenitta.blackjack

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.BlackjackSinglePlayerMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.StoredBlackjackRefundTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*

class BlackjackManager(val loritta: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    // This is used to issue refunds when Loritta restarts, if the UUID is different than the current UUID AND the match isn't finished, it means that we should refund the user
    val uniqueId = UUID.randomUUID()

    fun startRefundBlackjacksTask() {
        GlobalScope.launch {
            refundBlackjacks()
        }
    }

    private suspend fun refundBlackjacks() {
        logger.info { "Checking if there is any blackjack matches to be refunded..." }

        val (refundedBlackjacks, refundedBlackjacksNoBets) = loritta.transaction {
            var refundedBlackjacks = 0
            var refundedBlackjacksNoBets = 0

            val expiredMatches = BlackjackSinglePlayerMatches.selectAll()
                .where {
                    BlackjackSinglePlayerMatches.finishedAt.isNull() and (BlackjackSinglePlayerMatches.lorittaClusterId eq loritta.clusterId) and (BlackjackSinglePlayerMatches.blackjackManagerUniqueId neq this@BlackjackManager.uniqueId)
                }
                .toList()

            for (match in expiredMatches) {
                val now = Instant.now()

                val payoutValue = match[BlackjackSinglePlayerMatches.bet]
                logger.info { "Refunding match ${match[BlackjackSinglePlayerMatches.id]} because the stored UUID is ${match[BlackjackSinglePlayerMatches.blackjackManagerUniqueId]} while the current active UUID is ${this@BlackjackManager.uniqueId} - Refund should be $payoutValue" }

                if (payoutValue != null) {
                    BlackjackSinglePlayerMatches.update({ BlackjackSinglePlayerMatches.id eq match[BlackjackSinglePlayerMatches.id] }) {
                        it[BlackjackSinglePlayerMatches.finishedAt] = now
                        it[BlackjackSinglePlayerMatches.payout] = payoutValue
                        it[BlackjackSinglePlayerMatches.refunded] = true
                    }

                    Profiles.update({ Profiles.id eq match[BlackjackSinglePlayerMatches.id] }) {
                        it[Profiles.money] = Profiles.money + payoutValue
                    }

                    SimpleSonhosTransactionsLogUtils.insert(
                        match[BlackjackSinglePlayerMatches.user],
                        now,
                        TransactionType.BLACKJACK,
                        payoutValue, // We pay as is
                        StoredBlackjackRefundTransaction(match[BlackjackSinglePlayerMatches.id].value)
                    )

                    refundedBlackjacks++
                } else {
                    BlackjackSinglePlayerMatches.update({ BlackjackSinglePlayerMatches.id eq match[BlackjackSinglePlayerMatches.id] }) {
                        it[BlackjackSinglePlayerMatches.finishedAt] = now
                    }

                    refundedBlackjacksNoBets++
                }
            }

            Pair(refundedBlackjacks, refundedBlackjacksNoBets)
        }

        logger.info { "Refunded $refundedBlackjacks blackjacks and $refundedBlackjacksNoBets blackjacks without bets!" }
    }
}