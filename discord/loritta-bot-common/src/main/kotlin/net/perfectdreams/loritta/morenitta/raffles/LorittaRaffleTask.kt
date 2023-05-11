package net.perfectdreams.loritta.morenitta.raffles

import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.RaffleRewardSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.RaffleTicketsSonhosTransactionsLog
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.*
import java.awt.Color
import java.io.File
import java.time.Instant
import kotlin.time.toJavaDuration

class LorittaRaffleTask(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun run() {
        // TODO: Locales, maybe get the preferred user locale ID?
        val locale = m.localeManager.getLocaleById("default")

        val dmsToBeSent = m.transaction {
            val dmsToBeSent = mutableListOf<RaffleDM>()

            val now = Instant.now()

            // Get current active raffles
            val currentRaffles = Raffles.select {
                Raffles.endedAt.isNull()
            }.orderBy(Raffles.endsAt, SortOrder.DESC)
                .toList()

            for (currentRaffle in currentRaffles) {
                // Check if the current (not expired) raffle should have already ended
                if (now >= currentRaffle[Raffles.endsAt]) {
                    // Get all tickets
                    val tickets = RaffleTickets.select {
                        RaffleTickets.raffle eq currentRaffle[Raffles.id]
                    }.toList()

                    // Check if there are at least any ticket on the raffle and, if it is, then we process and get the winner
                    var winnerTicketId: Long? = null
                    var paidOutPrize: Long? = null

                    if (tickets.isNotEmpty()) {
                        // Get the winner of this raffle...
                        val winnerTicket = tickets[LorittaBot.RANDOM.nextInt(tickets.size)]
                        val winnerId = winnerTicket[RaffleTickets.userId]
                        winnerTicketId = winnerTicket[RaffleTickets.id].value

                        // Okay, so we found out who won the raffle
                        val currentActiveDonations = m.getActiveMoneyFromDonations(winnerId)
                        val plan = UserPremiumPlans.getPlanFromValue(currentActiveDonations)

                        val moneyWithoutTaxes = tickets.size * 250
                        val money = (moneyWithoutTaxes * plan.totalLoraffleReward).toInt()

                        val lorittaProfile = m.getOrCreateLorittaProfile(winnerId)
                        logger.info { "${winnerId} won $money sonhos ($moneyWithoutTaxes without taxes; before they had ${lorittaProfile.money} sonhos) in the raffle ${currentRaffle[Raffles.id]} (${currentRaffle[Raffles.raffleType]})!" }

                        val totalTicketsBoughtByTheUser = tickets.count { it[RaffleTickets.userId] == winnerId }
                        val totalTickets = tickets.size
                        val totalUsersInTheRaffle = tickets.map { it[RaffleTickets.userId] }.distinct().size

                        paidOutPrize = money.toLong()

                        lorittaProfile.addSonhosAndAddToTransactionLogNested(
                            money.toLong(),
                            SonhosPaymentReason.RAFFLE
                        )

                        val transactionLogId = SonhosTransactionsLog.insertAndGetId {
                            it[user] = winnerId
                            it[timestamp] = Instant.now()
                        }

                        RaffleRewardSonhosTransactionsLog.insert {
                            it[timestampLog] = transactionLogId
                            it[raffle] = currentRaffle[Raffles.id]
                        }

                        dmsToBeSent.add(
                            RaffleDM(
                                winnerId,
                                money,
                                totalTicketsBoughtByTheUser,
                                totalUsersInTheRaffle,
                                totalTickets
                            )
                        )
                    } else {
                        logger.info { "No one participated in the raffle ${currentRaffle[Raffles.id]} (${currentRaffle[Raffles.raffleType]})..." }
                    }

                    // Update the raffle to set its end time
                    Raffles.update({ Raffles.id eq currentRaffle[Raffles.id] }) {
                        it[Raffles.winnerTicket] = winnerTicketId
                        it[Raffles.endedAt] = now
                        it[Raffles.paidOutPrize] = paidOutPrize
                    }

                    // Now that the previous raffle has ended, let's create a new raffle of this type!
                    Raffles.insert {
                        it[Raffles.raffleType] = currentRaffle[Raffles.raffleType]
                        it[Raffles.startedAt] = now
                        it[Raffles.endsAt] = (now + currentRaffle[Raffles.raffleType].raffleDuration.toJavaDuration())
                        it[Raffles.endedAt] = null
                        it[Raffles.winnerTicket] = null
                        it[Raffles.paidOutPrize] = null
                    }
                }
            }

            for (type in RaffleType.values()) {
                val hasRaffleTypeCreated = currentRaffles.any { it[Raffles.raffleType] == type }

                if (!hasRaffleTypeCreated) {
                    // Create new raffle for this type!
                    Raffles.insert {
                        it[Raffles.raffleType] = type
                        it[Raffles.startedAt] = now
                        it[Raffles.endsAt] = (now + type.raffleDuration.toJavaDuration())
                        it[Raffles.endedAt] = null
                        it[Raffles.winnerTicket] = null
                        it[Raffles.paidOutPrize] = null
                    }
                }
            }

            return@transaction dmsToBeSent
        }

        for (raffleDM in dmsToBeSent) {
            val user = m.lorittaShards.retrieveUserById(raffleDM.winnerId)

            if (user != null && !user.isBot) {
                try {
                    val embed = EmbedBuilder()
                    embed.setThumbnail("attachment://loritta_money.png")
                    embed.setColor(Color(47, 182, 92))
                    embed.setTitle("\uD83C\uDF89 ${locale["commands.command.raffle.victory.title"]}!")
                    embed.setDescription(
                        locale.getList(
                            "commands.command.raffle.victory.description",
                            raffleDM.totalTicketsBoughtByTheUser,
                            raffleDM.money,
                            raffleDM.totalUsersInTheRaffle,
                            raffleDM.totalTickets,
                            raffleDM.totalTicketsBoughtByTheUser / raffleDM.totalTickets.toDouble(),
                            Emotes.LORI_RICH,
                            Emotes.LORI_NICE
                        ).joinToString("\n")
                    )

                    embed.setTimestamp(Instant.now())
                    val message = MessageCreateBuilder()
                        .setContent(" ")
                        .setEmbeds(embed.build())
                        .addFiles(FileUpload.fromData(File(LorittaBot.ASSETS, "loritta_money_discord.png"), "loritta_money.png"))
                        .build()
                    user.openPrivateChannel().queue {
                        it.sendMessage(message)
                            .queue()
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    data class RaffleDM(
        val winnerId: Long,
        val money: Int,
        val totalTicketsBoughtByTheUser: Int,
        val totalUsersInTheRaffle: Int,
        val totalTickets: Int
    )
}