package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.BrokerSonhosTransactionsLog
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
        if (TransactionType.SPARKLYPOWER_LSX in transactionTypeFilter)
            it.leftJoin(SparklyPowerLSXSonhosTransactionsLog)
        else
            it
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
                    TransactionType.SPARKLYPOWER_LSX -> cond.or(SparklyPowerLSXSonhosTransactionsLog.id.isNotNull())
                }
            }

            (SonhosTransactionsLog.user eq userId.value.toLong()).and(cond)
        }
}