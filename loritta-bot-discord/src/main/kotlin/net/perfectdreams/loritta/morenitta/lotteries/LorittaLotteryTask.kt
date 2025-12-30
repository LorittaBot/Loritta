package net.perfectdreams.loritta.morenitta.lotteries

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries.Lotteries
import net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries.LotteryTicketNumbers
import net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries.LotteryTickets
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.lottery.LotteryUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.serializable.StoredLotteryRewardTransaction
import net.perfectdreams.loritta.serializable.StoredLotteryTicketsTransaction
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.awt.Color
import java.time.OffsetDateTime

class LorittaLotteryTask(val m: LorittaBot) : RunnableCoroutine {
    override suspend fun run() {
        // TODO: Actually use proper i18n!
        val i18nContext = m.languageManager.defaultI18nContext

        val dmsToBeSent = m.lottery.mutex.withLock {
            m.transaction {
                val dmsToBeSent = mutableListOf<LotteryDM>()

                val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

                // Get current active lotteries
                val currentLotteries = Lotteries.selectAll()
                    .where {
                        Lotteries.endedAt.isNull()
                    }.orderBy(Lotteries.endsAt, SortOrder.DESC)
                    .toList()

                for (currentLottery in currentLotteries) {
                    // Check if the current (not expired) raffle should have already ended
                    if (now >= currentLottery[Lotteries.endsAt]) {
                        val winningNumbers = mutableListOf<Int>()

                        while (winningNumbers.size != currentLottery[Lotteries.numbersPerTicket]) {
                            val number = m.random.nextInt(1, currentLottery[Lotteries.tableTotalNumbers] + 1)

                            if (winningNumbers.contains(number))
                                continue

                            winningNumbers.add(number)
                        }

                        val totalTickets = LotteryTickets.selectAll()
                            .where {
                                LotteryTickets.lottery eq currentLottery[Lotteries.id]
                            }
                            .count()

                        var hits: Int? = null

                        if (totalTickets != 0L) {
                            // How does it work?
                            //
                            // We do a "top to bottom" where we will attempt to match winners like this:
                            // 1. Did someone get all the numbers right?
                            // 2. If not... did someone get all - 1 numbers right?
                            // 3. So on and so forth...
                            for (requiredNumbersToGet in currentLottery[Lotteries.numbersPerTicket] downTo 1) {
                                val winningTickets = LotteryTicketNumbers
                                    .innerJoin(LotteryTickets)
                                    .select(LotteryTickets.id, LotteryTicketNumbers.ticket, LotteryTickets.lottery, LotteryTickets.userId)
                                    .where {
                                        LotteryTickets.lottery eq currentLottery[Lotteries.id] and (LotteryTicketNumbers.number inList winningNumbers)
                                    }
                                    .groupBy(LotteryTickets.id, LotteryTicketNumbers.ticket, LotteryTickets.lottery, LotteryTickets.userId)
                                    .having { LotteryTicketNumbers.number.count() eq requiredNumbersToGet.toLong() }
                                    .toList()

                                val totalPayout = currentLottery[Lotteries.ticketPrice] * totalTickets

                                if (winningTickets.isNotEmpty()) {
                                    // Set ticket winners
                                    LotteryTickets.update({ LotteryTickets.id inList winningTickets.map { it[LotteryTickets.id] } }) {
                                        it[LotteryTickets.winner] = true
                                    }

                                    val ticketNumbersOfTheWinners = LotteryTicketNumbers.selectAll()
                                        .where {
                                            LotteryTicketNumbers.ticket inList winningTickets.map { it[LotteryTicketNumbers.ticket] }
                                        }
                                        .toList()

                                    val payoutForEachTicket = totalPayout / winningTickets.size

                                    val grouped = winningTickets.groupBy { it[LotteryTickets.userId] }
                                    for ((userId, tickets) in grouped) {
                                        val payoutForUserTicketsWithoutTaxes = (tickets.size * payoutForEachTicket)

                                        // Okay, so now we need to figure out if the user is taxable or not
                                        val currentActiveDonations = m.getActiveMoneyFromDonations(userId)
                                        val plan = UserPremiumPlans.getPlanFromValue(currentActiveDonations)

                                        val payoutForUserTicketsWithTaxes = (payoutForUserTicketsWithoutTaxes * plan.totalLotteryReward).toLong()

                                        Profiles.update({ Profiles.id eq userId }) {
                                            it[Profiles.money] = Profiles.money + payoutForUserTicketsWithTaxes
                                        }

                                        SimpleSonhosTransactionsLogUtils.insert(
                                            userId,
                                            now.toInstant(),
                                            TransactionType.LOTTERY,
                                            payoutForUserTicketsWithTaxes, // We pay as is
                                            StoredLotteryRewardTransaction(
                                                currentLottery[Lotteries.id].value,
                                                plan.totalLoraffleReward != 1.0,
                                                payoutForUserTicketsWithoutTaxes
                                            )
                                        )

                                        val ticketNumbers = mutableListOf<List<Int>>()

                                        for (ticket in tickets) {
                                            ticketNumbers.add(
                                                ticketNumbersOfTheWinners.filter {
                                                    it[LotteryTicketNumbers.ticket] == ticket[LotteryTicketNumbers.ticket]
                                                }.map { it[LotteryTicketNumbers.number] }
                                            )
                                        }

                                        dmsToBeSent.add(
                                            LotteryDM.WonTheLottery(
                                                userId,
                                                requiredNumbersToGet,
                                                grouped.size,
                                                payoutForUserTicketsWithTaxes,
                                                winningNumbers,
                                                ticketNumbers
                                            )
                                        )

                                        hits = requiredNumbersToGet
                                    }
                                    break
                                }
                            }
                        }

                        Lotteries.update({ Lotteries.id eq currentLottery[Lotteries.id] }) {
                            it[Lotteries.endedAt] = now
                            it[Lotteries.winningNumbers] = winningNumbers
                            it[Lotteries.hits] = hits
                        }
                    }
                }

                dmsToBeSent
            }
        }

        for (lotteryDM in dmsToBeSent) {
            try {
                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(lotteryDM.userId)

                if (privateChannel != null) {
                    val message = when (lotteryDM) {
                        is LotteryDM.WonTheLottery -> {
                            MessageCreateBuilder {
                                content = " "
                                this.useComponentsV2 = true

                                container {
                                    this.accentColor = Color(47, 182, 92)

                                    this.text(
                                        buildString {
                                            appendLine("## ${i18nContext.get(I18nKeysData.Lottery.YouWonTheLottery)}")

                                            appendLine()
                                            appendLine(i18nContext.get(I18nKeysData.Lottery.DrawnNumbers(LotteryUtils.formatTicketNumbers(lotteryDM.winningNumbers))))
                                            appendLine()
                                            if (lotteryDM.totalUsers == 1) {
                                                appendLine(i18nContext.get(I18nKeysData.Lottery.YouHitNumbers(lotteryDM.hits)))
                                            } else {
                                                appendLine(i18nContext.get(I18nKeysData.Lottery.YouAndXMoreUsersHitNumbers(lotteryDM.totalUsers - 1, lotteryDM.hits)))
                                            }
                                            appendLine()
                                            appendLine(i18nContext.get(I18nKeysData.Lottery.YouWonXSonhos(SonhosUtils.getSonhosEmojiOfQuantity(lotteryDM.money), lotteryDM.money)))
                                            appendLine()
                                            appendLine(i18nContext.get(I18nKeysData.Lottery.LuckyTickets(lotteryDM.winningTicketNumbers.size)))
                                            for (ticket in lotteryDM.winningTicketNumbers.take(25)) {
                                                appendLine(LotteryUtils.formatTicketNumbers(ticket))
                                            }
                                            if (lotteryDM.winningTicketNumbers.size > 25) {
                                                appendLine(i18nContext.get(I18nKeysData.Lottery.AndXTicketsMore(lotteryDM.winningTicketNumbers.size - 25)))
                                            }
                                        }
                                    )
                                }
                            }.build()
                        }
                    }

                    privateChannel.sendMessage(message).queue()
                }
            } catch (e: Exception) {
            }
        }
    }

    sealed class LotteryDM {
        abstract val userId: Long

        class WonTheLottery(
            override val userId: Long,
            val hits: Int,
            val totalUsers: Int,
            val money: Long,
            val winningNumbers: List<Int>,
            val winningTicketNumbers: List<List<Int>>,
        ) : LotteryDM()
    }
}