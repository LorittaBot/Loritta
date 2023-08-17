package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.Daily
import net.perfectdreams.loritta.serializable.EmojiFightBetSonhosTransaction
import net.perfectdreams.loritta.serializable.SonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.*
import net.perfectdreams.loritta.common.utils.TransactionType
import org.jetbrains.exposed.sql.*
import kotlin.time.Duration.Companion.days

class SonhosService(private val pudding: Pudding) : Service(pudding) {
    /**
     * Gets the rank position in the sonhos's leaderboard, based on the [sonhos] amount
     *
     * @return in what rank position the user is
     */
    // TODO: This is not a *good* way to get an user's ranking if there are duplicates, maybe use DENSE_RANK? https://www.postgresqltutorial.com/postgresql-dense_rank-function/
    suspend fun getSonhosRankPositionBySonhos(sonhos: Long) = pudding.transaction {
        Profiles.select { Profiles.money greaterEq sonhos and (Profiles.id notInSubQuery UsersService.validBannedUsersList(System.currentTimeMillis())) }
            .count()
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
                .map {
                    // ===[ SPECIAL CASES ]===
                    // We need to query how many users lost the bet
                    if (it.getOrNull(EmojiFightSonhosTransactionsLog.id) != null) {
                        val usersInMatch = EmojiFightParticipants.select { EmojiFightParticipants.match eq it[EmojiFightParticipants.match] }
                            .count()

                        EmojiFightBetSonhosTransaction(
                            it[SonhosTransactionsLog.id].value,
                            it[SonhosTransactionsLog.timestamp].toKotlinInstant(),
                            UserId(it[SonhosTransactionsLog.user].value),
                            UserId(it[EmojiFightParticipants.user].value),
                            usersInMatch,
                            it[EmojiFightParticipants.emoji],
                            it[EmojiFightMatchmakingResults.entryPrice],
                            it[EmojiFightMatchmakingResults.entryPriceAfterTax],
                            it[EmojiFightMatchmakingResults.tax],
                            it[EmojiFightMatchmakingResults.taxPercentage]
                        )
                    } else {
                        SonhosTransaction.fromRow(it)
                    }
                }
        }
    }

    // If we want to filter for specific transactions, check if the table ID is null!
    // Example: BrokerSonhosTransactionsLog.id isNotNull
    private fun userTransactionQuery(
        userId: UserId,
        transactionTypeFilter: List<TransactionType>
    ) = SonhosTransactionsLog.let {
        if (TransactionType.PAYMENT in transactionTypeFilter)
            it.leftJoin(PaymentSonhosTransactionsLog.leftJoin(PaymentSonhosTransactionResults))
        else it
    }.let {
        if (TransactionType.DAILY_REWARD in transactionTypeFilter)
            it.leftJoin(DailyRewardSonhosTransactionsLog.leftJoin(Dailies))
        else it
    }.let {
        if (TransactionType.HOME_BROKER in transactionTypeFilter)
            it.leftJoin(BrokerSonhosTransactionsLog)
        else it
    }.let {
        if (TransactionType.COINFLIP_BET in transactionTypeFilter)
            it.leftJoin(CoinFlipBetSonhosTransactionsLog.leftJoin(CoinFlipBetMatchmakingResults))
        else it
    }.let {
        if (TransactionType.COINFLIP_BET_GLOBAL in transactionTypeFilter)
            it.leftJoin(CoinFlipBetGlobalSonhosTransactionsLog.leftJoin(CoinFlipBetGlobalMatchmakingResults))
        else it
    }.let {
        if (TransactionType.EMOJI_FIGHT_BET in transactionTypeFilter)
            it.leftJoin(EmojiFightSonhosTransactionsLog.leftJoin(EmojiFightMatchmakingResults.leftJoin(EmojiFightParticipants)))
        else it
    }.let {
        val r1 = Raffles.alias("r1")
        val r2 = Raffles.alias("r2")

        if (TransactionType.RAFFLE in transactionTypeFilter)
            it.leftJoin(RaffleRewardSonhosTransactionsLog.leftJoin(r1, { RaffleRewardSonhosTransactionsLog.raffle }, { this[Raffles.id] }))
                .leftJoin(RaffleTicketsSonhosTransactionsLog.leftJoin(r2, { RaffleTicketsSonhosTransactionsLog.raffle }, { this[Raffles.id] }))
        else it
    }.let {
        if (TransactionType.SPARKLYPOWER_LSX in transactionTypeFilter)
            it.leftJoin(SparklyPowerLSXSonhosTransactionsLog)
        else it
    }.let {
        if (TransactionType.SONHOS_BUNDLE_PURCHASE in transactionTypeFilter)
            it.leftJoin(SonhosBundlePurchaseSonhosTransactionsLog.leftJoin(SonhosBundles))
        else it
    }.let {
        if (TransactionType.INACTIVE_DAILY_TAX in transactionTypeFilter)
            it.leftJoin(DailyTaxSonhosTransactionsLog)
        else it
    }.let {
        if (TransactionType.DIVINE_INTERVENTION in transactionTypeFilter)
            it.leftJoin(DivineInterventionSonhosTransactionsLog)
        else it
    }.let {
        if (TransactionType.BOT_VOTE in transactionTypeFilter)
            it.leftJoin(BotVoteSonhosTransactionsLog)
        else it
    }.let {
        if (TransactionType.SHIP_EFFECT in transactionTypeFilter)
            it.leftJoin(ShipEffectSonhosTransactionsLog)
        else it
    }.let {
        if (TransactionType.EVENTS in transactionTypeFilter)
            it.leftJoin(Christmas2022SonhosTransactionsLog)
                .leftJoin(Easter2023SonhosTransactionsLog)
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
                    TransactionType.PAYMENT -> cond.or(PaymentSonhosTransactionsLog.id.isNotNull())
                    TransactionType.DAILY_REWARD -> cond.or(DailyRewardSonhosTransactionsLog.id.isNotNull())
                    TransactionType.HOME_BROKER -> cond.or(BrokerSonhosTransactionsLog.id.isNotNull())
                    TransactionType.COINFLIP_BET -> cond.or(CoinFlipBetSonhosTransactionsLog.id.isNotNull())
                    TransactionType.COINFLIP_BET_GLOBAL -> cond.or(CoinFlipBetGlobalSonhosTransactionsLog.id.isNotNull())
                    TransactionType.EMOJI_FIGHT_BET -> cond.or(EmojiFightSonhosTransactionsLog.id.isNotNull())
                    TransactionType.RAFFLE -> cond
                        .or(RaffleTicketsSonhosTransactionsLog.id.isNotNull())
                        .or(RaffleRewardSonhosTransactionsLog.id.isNotNull())
                    TransactionType.SPARKLYPOWER_LSX -> cond.or(SparklyPowerLSXSonhosTransactionsLog.id.isNotNull())
                    TransactionType.SONHOS_BUNDLE_PURCHASE -> cond.or(SonhosBundlePurchaseSonhosTransactionsLog.id.isNotNull())
                    TransactionType.INACTIVE_DAILY_TAX -> cond.or(DailyTaxSonhosTransactionsLog.id.isNotNull())
                    TransactionType.DIVINE_INTERVENTION -> cond.or(DivineInterventionSonhosTransactionsLog.id.isNotNull())
                    TransactionType.BOT_VOTE -> cond.or(BotVoteSonhosTransactionsLog.id.isNotNull())
                    TransactionType.SHIP_EFFECT -> cond.or(ShipEffectSonhosTransactionsLog.id.isNotNull())
                    TransactionType.EVENTS -> cond
                        .or(Christmas2022SonhosTransactionsLog.id.isNotNull())
                        .or(Easter2023SonhosTransactionsLog.id.isNotNull())
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
            val timeInMillis = afterTime.toEpochMilliseconds()

            val dailyResult = Dailies.select {
                Dailies.receivedById eq userId.value.toLong() and (Dailies.receivedAt greaterEq timeInMillis)
            }
                .orderBy(Dailies.receivedAt, SortOrder.DESC)
                .limit(1)
                .firstOrNull()

            return@transaction if (dailyResult != null)
                Daily.fromRow(dailyResult)
            else null
        }
    }

    suspend fun userGotDailyRecently(userId: Long, numberOfDays: Int): Boolean {
        return getUserLastDailyRewardReceived(
            UserId(userId),
            Clock.System.now() - numberOfDays.days
        ) != null
    }
}