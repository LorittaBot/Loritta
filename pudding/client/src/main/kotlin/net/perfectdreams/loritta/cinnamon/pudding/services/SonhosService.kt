package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.BrokerSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.sql.SortOrder
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
        SonhosTransactionsLog.let {
            if (TransactionType.HOME_BROKER in transactionTypeFilter)
                it.leftJoin(BrokerSonhosTransactionsLog)
            else
                it
        }
            .select {
                (SonhosTransactionsLog.user eq userId.value.toLong()).let {
                    if (TransactionType.HOME_BROKER in transactionTypeFilter)
                        BrokerSonhosTransactionsLog.id.isNotNull()
                    else
                        it
                }
            }.count()
    }

    suspend fun getUserTransactions(
        userId: UserId,
        transactionTypeFilter: List<TransactionType>,
        limit: Int,
        offset: Long
    ): List<SonhosTransaction> {
        return pudding.transaction {
            // If we want to filter for specific transactions, check if the table ID is null!
            // Example: BrokerSonhosTransactionsLog.id isNotNull
            SonhosTransactionsLog.let {
                if (TransactionType.HOME_BROKER in transactionTypeFilter)
                    it.leftJoin(BrokerSonhosTransactionsLog)
                else
                    it
            }
                .select {
                    (SonhosTransactionsLog.user eq userId.value.toLong()).let {
                        if (TransactionType.HOME_BROKER in transactionTypeFilter)
                            BrokerSonhosTransactionsLog.id.isNotNull()
                        else
                            it
                    }
                }
                .orderBy(SonhosTransactionsLog.id, SortOrder.DESC)
                .limit(limit, offset)
                .map { SonhosTransaction.fromRow(it) }
        }
    }
}