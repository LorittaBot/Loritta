package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Daily
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.BrokerSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.SparklyPowerLSXSonhosTransactionsLog
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select

class SonhosService(private val pudding: Pudding) : Service(pudding) {
    /**
     * Gets the rank position in the sonhos's leaderboard, based on the [sonhos] amount
     *
     * @return in what rank position the user is
     */
    suspend fun getSonhosRankPositionBySonhos(sonhos: Long): Long {
        // TODO: This is not a *good* way to get an user's ranking if there are duplicates, maybe use DENSE_RANK? https://www.postgresqltutorial.com/postgresql-dense_rank-function/
        val position = pudding.transaction {
            Profiles.select { Profiles.money greaterEq sonhos }
                .count()
        }

        return position
    }

    suspend fun getUserTotalTransactions(
        userId: UserId,
        transactionTypeFilter: List<TransactionType>
    ) = pudding.transaction {
        userTransactionQuery(userId, transactionTypeFilter).count()
    }

    suspend fun getUserTransactions(
        userId: UserId,
        transactionTypeFilter: List<TransactionType>,
        limit: Int,
        offset: Long
    ): List<SonhosTransaction> {
        return pudding.transaction {
            userTransactionQuery(userId, transactionTypeFilter)
                .orderBy(SonhosTransactionsLog.id, SortOrder.DESC)
                .limit(limit, offset)
                .map { SonhosTransaction.fromRow(it) }
        }
    }

    // If we want to filter for specific transactions, check if the table ID is null!
    // Example: BrokerSonhosTransactionsLog.id isNotNull
    private fun userTransactionQuery(
        userId: UserId,
        transactionTypeFilter: List<TransactionType>
    ) = SonhosTransactionsLog.let {
        if (TransactionType.HOME_BROKER in transactionTypeFilter)
            it.leftJoin(BrokerSonhosTransactionsLog)
        else it
    }.let {
        if (TransactionType.COINFLIP_BET_GLOBAL in transactionTypeFilter)
            it.leftJoin(CoinFlipBetGlobalSonhosTransactionsLog.leftJoin(CoinFlipBetGlobalMatchmakingResults))
        else it
    }.let {
        if (TransactionType.SPARKLYPOWER_LSX in transactionTypeFilter)
            it.leftJoin(SparklyPowerLSXSonhosTransactionsLog)
        else it
    }
        .select {
            // Hacky!
            // https://stackoverflow.com/questions/54361503/how-to-add-multiple-or-filter-conditions-based-on-incoming-parameters-using-expo
            var cond = Op.build {
                SonhosTransactionsLog.id neq SonhosTransactionsLog.id
            }

            for (type in transactionTypeFilter) {
                cond = when (type) {
                    TransactionType.HOME_BROKER -> cond.or(BrokerSonhosTransactionsLog.id.isNotNull())
                    TransactionType.COINFLIP_BET_GLOBAL -> cond.or(CoinFlipBetGlobalSonhosTransactionsLog.id.isNotNull())
                    TransactionType.SPARKLYPOWER_LSX -> cond.or(SparklyPowerLSXSonhosTransactionsLog.id.isNotNull())
                }
            }

            (SonhosTransactionsLog.user eq userId.value.toLong()).and(cond)
        }

    /**
     * Gets the user's last received daily reward
     *
     * @param userId    the user's ID
     * @param afterTime allows filtering dailies by time, only dailies [afterTime] will be retrieven
     * @return the last received daily reward, if it exists
     */
    suspend fun getUserLastDailyRewardReceived(userId: UserId, afterTime: kotlinx.datetime.Instant): Daily? {
        return pudding.transaction {
            _getUserLastDailyRewardReceived(userId, afterTime)
        }
    }

    /**
     * Gets the user's last received daily reward
     *
     * @param userId    the user's ID
     * @param afterTime allows filtering dailies by time, only dailies [afterTime] will be retrieven
     * @return the last received daily reward, if it exists
     */
    internal fun _getUserLastDailyRewardReceived(userId: UserId, afterTime: kotlinx.datetime.Instant): Daily? {
        val timeInMillis = afterTime.toEpochMilliseconds()

        val dailyResult = Dailies.select {
            Dailies.receivedById eq userId.value.toLong() and (Dailies.receivedAt greaterEq timeInMillis)
        }
            .orderBy(Dailies.receivedAt, SortOrder.DESC)
            .limit(1)
            .firstOrNull()

        return if (dailyResult != null)
            Daily.fromRow(dailyResult)
        else null
    }
}