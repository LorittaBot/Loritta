package net.perfectdreams.loritta.morenitta.raffles

import kotlinx.coroutines.sync.withLock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.UserAskedRaffleNotifications
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredRaffleRewardTransaction
import org.jetbrains.exposed.sql.*
import java.awt.Color
import java.io.File
import java.time.Instant
import kotlin.time.toJavaDuration

class LorittaRaffleTask(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun run() {
        // TODO: Locales, maybe get the preferred user locale ID?
        val locale = m.localeManager.getLocaleById("default")
        val i18nContext = m.languageManager.defaultI18nContext

        // Before, we used TRANSACTION_SERIALIZABLE because REPEATABLE READ will cause issues if someone buys raffle tickets when the LorittaRaffleTask is processing the current raffle winners
        // However, SERIALIZABLE is also a bit bad since we need to fully run the transaction separetely from everything
        // To workaround this, we will use a coroutine mutex, yay!
        // This way, we don't block all transactions, while still letting other transactions work
        val dmsToBeSent = m.raffleResultsMutex.withLock {
            m.transaction {
                val dmsToBeSent = mutableListOf<RaffleDM>()

                val now = Instant.now()

                // Get current active raffles
                val currentRaffles = Raffles.selectAll()
                    .where {
                        Raffles.endedAt.isNull()
                    }.orderBy(Raffles.endsAt, SortOrder.DESC)
                    .toList()

                for (currentRaffle in currentRaffles) {
                    // Check if the current (not expired) raffle should have already ended
                    if (now >= currentRaffle[Raffles.endsAt]) {
                        logger.info { "Getting the results of the raffle ${currentRaffle[Raffles.id]} (${currentRaffle[Raffles.raffleType]})" }

                        // Get all tickets on the current raffle
                        val totalTickets = RaffleTickets.selectAll().where {
                            RaffleTickets.raffle eq currentRaffle[Raffles.id]
                        }.count()

                        logger.info { "Raffle ${currentRaffle[Raffles.id]} (${currentRaffle[Raffles.raffleType]}) has $totalTickets tickets!" }

                        var winnerTicketId: Long? = null
                        var paidOutPrize: Long? = null
                        var paidOutPrizeAfterTax: Long? = null
                        var tax: Long? = null
                        var taxPercentage: Double? = null

                        if (totalTickets != 0L) {
                            // Generate a random number
                            val skipTickets = m.random.nextLong(0, totalTickets)

                            // Now here's the magic:
                            // Get the winner ticket

                            // Using DESC is WAY FASTER than ASC, since using DESC PostgreSQL filters the table backwards
                            // loritta=# explain analyze select * from RaffleTickets where raffle = 21415 order by id desc limit 1;
                            //                                                                         QUERY PLAN
                            // -------------------------------------------------------------------------------------------------------------------------------------------------------------
                            //  Limit  (cost=0.57..164.60 rows=1 width=32) (actual time=0.198..0.199 rows=1 loops=1)
                            //    ->  Index Scan Backward using raffletickets_pkey on raffletickets  (cost=0.57..13591837.22 rows=82862 width=32) (actual time=0.198..0.198 rows=1 loops=1)
                            //          Filter: (raffle = 21415)
                            //          Rows Removed by Filter: 1740
                            //  Planning Time: 0.057 ms
                            //  Execution Time: 0.205 ms
                            // (6 rows)
                            //
                            // loritta=# explain analyze select * from RaffleTickets where raffle = 21415 order by id asc limit 1;
                            //                                                                         QUERY PLAN
                            // ------------------------------------------------------------------------------------------------------------------------------------------------------------
                            //  Limit  (cost=0.57..164.60 rows=1 width=32) (actual time=56413.256..56413.257 rows=1 loops=1)
                            //    ->  Index Scan using raffletickets_pkey on raffletickets  (cost=0.57..13591837.22 rows=82862 width=32) (actual time=56413.255..56413.256 rows=1 loops=1)
                            //          Filter: (raffle = 21415)
                            //          Rows Removed by Filter: 485757618
                            //  Planning Time: 0.040 ms
                            //  Execution Time: 56413.265 ms
                            // (6 rows)

                            // We need to sort by bought at because that, for some reason, is faster when doing LIMIT 1 OFFSET 20
                            // When sorting by the ID, it gets wonky as hell with some offset values, with some of them taking 5 minutes+ to process for some reason?
                            val winnerTicket = RaffleTickets.selectAll()
                                .where {
                                    RaffleTickets.raffle eq currentRaffle[Raffles.id]
                                }.orderBy(RaffleTickets.boughtAt, SortOrder.DESC)
                                .limit(1)
                                .offset(skipTickets)
                                .first()

                            logger.info { "Raffle ${currentRaffle[Raffles.id]} (${currentRaffle[Raffles.raffleType]}) winner ticket is ticket ${winnerTicket[RaffleTickets.id]} by ${winnerTicket[RaffleTickets.userId]}!" }

                            // Check if there are at least any ticket on the raffle and, if it is, then we process and get the winner
                            // Get the winner of this raffle...
                            val winnerId = winnerTicket[RaffleTickets.userId]
                            winnerTicketId = winnerTicket[RaffleTickets.id].value

                            // Okay, so we found out who won the raffle
                            val plan = m.getUserPremiumPlan(winnerId)

                            val moneyWithoutTaxes = totalTickets * 250
                            paidOutPrize = moneyWithoutTaxes.toLong()

                            val money = (moneyWithoutTaxes * plan.totalLoraffleReward).toInt()

                            val lorittaProfile = m.getOrCreateLorittaProfile(winnerId)
                            logger.info { "${winnerId} won $money sonhos ($moneyWithoutTaxes without taxes; before they had ${lorittaProfile.money} sonhos) in the raffle ${currentRaffle[Raffles.id]} (${currentRaffle[Raffles.raffleType]})!" }

                            val totalTicketsBoughtByTheUser = RaffleTickets.selectAll()
                                .where { RaffleTickets.raffle eq currentRaffle[Raffles.id] and (RaffleTickets.userId eq winnerId) }
                                .count()
                            val countUserDistinct = RaffleTickets.userId.countDistinct()
                            val totalUsersInTheRaffle = RaffleTickets.select(countUserDistinct)
                                .where { RaffleTickets.raffle eq currentRaffle[Raffles.id] }
                                .first()[countUserDistinct]

                            paidOutPrizeAfterTax = money.toLong()

                            tax = paidOutPrize - paidOutPrizeAfterTax
                            taxPercentage =
                                (1.0.toBigDecimal() - plan.totalLoraffleReward.toBigDecimal()).toDouble() // Avoid rounding errors

                            lorittaProfile.addSonhosAndAddToTransactionLogNested(
                                paidOutPrizeAfterTax,
                                SonhosPaymentReason.RAFFLE
                            )

                            // Cinnamon transaction log
                            SimpleSonhosTransactionsLogUtils.insert(
                                winnerId,
                                Instant.now(),
                                TransactionType.RAFFLE,
                                paidOutPrizeAfterTax,
                                StoredRaffleRewardTransaction(currentRaffle[Raffles.id].value)
                            )

                            dmsToBeSent.add(
                                RaffleDM.WonTheRaffle(
                                    winnerId,
                                    currentRaffle[Raffles.raffleType],
                                    money,
                                    totalTicketsBoughtByTheUser.toInt(),
                                    totalUsersInTheRaffle.toInt(),
                                    totalTickets.toInt()
                                )
                            )

                            // Get everyone that asked to be notified about this raffle (EXCEPT THE WINNER)
                            UserAskedRaffleNotifications.selectAll().where {
                                UserAskedRaffleNotifications.raffle eq currentRaffle[Raffles.id] and (UserAskedRaffleNotifications.userId neq winnerId)
                            }.toList()
                                .forEach {
                                    dmsToBeSent.add(
                                        RaffleDM.LostTheRaffle(
                                            it[UserAskedRaffleNotifications.userId],
                                            currentRaffle[Raffles.raffleType],
                                            winnerId,
                                            money,
                                            totalTicketsBoughtByTheUser.toInt(),
                                            totalUsersInTheRaffle.toInt(),
                                            totalTickets.toInt()
                                        )
                                    )
                                }
                        } else {
                            logger.info { "No one participated in the raffle ${currentRaffle[Raffles.id]} (${currentRaffle[Raffles.raffleType]})..." }

                            // Get everyone that asked to be notified about this raffle
                            UserAskedRaffleNotifications.selectAll().where {
                                UserAskedRaffleNotifications.raffle eq currentRaffle[Raffles.id]
                            }.toList()
                                .forEach {
                                    dmsToBeSent.add(
                                        RaffleDM.LostTheRaffleNoOneBoughtTickets(
                                            it[UserAskedRaffleNotifications.userId],
                                            currentRaffle[Raffles.raffleType]
                                        )
                                    )
                                }
                        }

                        // Update the raffle to set its end time
                        Raffles.update({ Raffles.id eq currentRaffle[Raffles.id] }) {
                            it[Raffles.winnerTicket] = winnerTicketId
                            it[Raffles.endedAt] = now
                            it[Raffles.paidOutPrize] = paidOutPrize
                            it[Raffles.paidOutPrizeAfterTax] = paidOutPrizeAfterTax

                            if (tax != null) {
                                it[Raffles.tax] = tax
                                it[Raffles.taxPercentage] = taxPercentage
                            } else {
                                it[Raffles.tax] = null
                                it[Raffles.taxPercentage] = null
                            }
                        }

                        // Now that the previous raffle has ended, let's create a new raffle of this type!
                        // But we only want to create a new raffle if the raffle is enabled!
                        if (currentRaffle[Raffles.raffleType].enabled) {
                            Raffles.insert {
                                it[Raffles.raffleType] = currentRaffle[Raffles.raffleType]
                                it[Raffles.startedAt] = now
                                if (currentRaffle[Raffles.raffleType] == RaffleType.DAILY) {
                                    // The end date of the daily raffle is a raffle is a bit different on how it is handled
                                    // It should ALWAYS start at 00:00 GMT-3... why? because it is fun!
                                    // First we get when the current raffle ended...
                                    val storedDateOfWhenTheCurrentRaffleEnded = currentRaffle[Raffles.endsAt]
                                    // Then we +1 and set it to midnight
                                    val storedDateTomorrowAtMidnight = storedDateOfWhenTheCurrentRaffleEnded.atZone(Constants.LORITTA_TIMEZONE)
                                        .plusDays(1)
                                        .withHour(0)
                                        .withMinute(0)
                                        .withSecond(0)
                                        .toInstant()
                                    it[Raffles.endsAt] = storedDateTomorrowAtMidnight
                                } else {
                                    it[Raffles.endsAt] = (now + currentRaffle[Raffles.raffleType].raffleDuration.toJavaDuration())
                                }
                                it[Raffles.endedAt] = null
                                it[Raffles.winnerTicket] = null
                                it[Raffles.paidOutPrize] = null
                                it[Raffles.tax] = null
                                it[Raffles.taxPercentage] = null
                            }
                        }
                    }
                }

                for (type in RaffleType.entries.filter { it.enabled }) {
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
        }

        for (raffleDM in dmsToBeSent) {
            try {
                val privateChannel = m.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(raffleDM.userId)

                if (privateChannel != null) {
                    val message = when (raffleDM) {
                        is RaffleDM.WonTheRaffle -> {
                            val embed = EmbedBuilder()
                            embed.setThumbnail("attachment://loritta_money.png")
                            embed.setColor(Color(47, 182, 92))
                            embed.setTitle(
                                "\uD83C\uDF89 ${locale["commands.command.raffle.victory.title"]}! - ${
                                    i18nContext.get(
                                        raffleDM.raffleType.title
                                    )
                                }"
                            )
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
                            MessageCreateBuilder()
                                .setContent(" ")
                                .setEmbeds(embed.build())
                                .addFiles(
                                    FileUpload.fromData(
                                        File(LorittaBot.ASSETS, "loritta_money_discord.png"),
                                        "loritta_money.png"
                                    )
                                )
                                .build()
                        }

                        is RaffleDM.LostTheRaffle -> {
                            val lastWinnerId = raffleDM.winnerId
                            val lastWinner = if (lastWinnerId != null) {
                                HarmonyLoggerFactory.logger {}.value.info { "LorittaRaffleTask#retrieveUserInfoById - UserId: ${lastWinnerId}" }
                                m.lorittaShards.retrieveUserInfoById(lastWinnerId.toLong())
                            } else {
                                null
                            }

                            val nameAndDiscriminator = if (lastWinner != null) {
                                (lastWinner.name + "#" + lastWinner.discriminator).let {
                                    if (MiscUtils.hasInvite(it))
                                        "¯\\_(ツ)_/¯"
                                    else
                                        it
                                }
                            } else {
                                "\uD83E\uDD37"
                            }.stripCodeMarks()

                            val i18nPrefix = I18nKeysData.Commands.Command.Raffle.DirectMessages.YouLost

                            val embed = EmbedBuilder()
                            embed.setThumbnail("https://stuff.loritta.website/emotes/lori-sob.png")
                            embed.setColor(Color(47, 182, 92))
                            embed.setTitle(
                                "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob} ${
                                    i18nContext.get(
                                        i18nPrefix.Title(i18nContext.get(raffleDM.raffleType.title))
                                    )
                                }"
                            )
                            embed.setDescription(
                                i18nContext.get(
                                    i18nPrefix.Description(
                                        "`$nameAndDiscriminator`",
                                        raffleDM.money
                                    )
                                ).joinToString("\n\n")
                            )

                            embed.setTimestamp(Instant.now())
                            MessageCreateBuilder()
                                .setContent(" ")
                                .setEmbeds(embed.build())
                                .build()
                        }

                        is RaffleDM.LostTheRaffleNoOneBoughtTickets -> {
                            val i18nPrefix =
                                I18nKeysData.Commands.Command.Raffle.DirectMessages.YouLostNoOneBoughtTickets

                            val embed = EmbedBuilder()
                            embed.setThumbnail("https://stuff.loritta.website/emotes/lori-zz.png")
                            embed.setColor(Color(47, 182, 92))
                            embed.setTitle(
                                "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob} ${
                                    i18nContext.get(
                                        i18nPrefix.Title(i18nContext.get(raffleDM.raffleType.title))
                                    )
                                }"
                            )
                            embed.setDescription(
                                i18nContext.get(i18nPrefix.Description).joinToString("\n\n")
                            )

                            embed.setTimestamp(Instant.now())
                            MessageCreateBuilder()
                                .setContent(" ")
                                .setEmbeds(embed.build())
                                .build()
                        }
                    }

                    privateChannel.sendMessage(message).queue()
                }
            } catch (e: Exception) {
            }
        }
    }

    sealed class RaffleDM {
        abstract val userId: Long
        abstract val raffleType: RaffleType

        class WonTheRaffle(
            override val userId: Long,
            override val raffleType: RaffleType,
            val money: Int,
            val totalTicketsBoughtByTheUser: Int,
            val totalUsersInTheRaffle: Int,
            val totalTickets: Int
        ) : RaffleDM()

        class LostTheRaffle(
            override val userId: Long,
            override val raffleType: RaffleType,
            val winnerId: Long,
            val money: Int,
            val totalTicketsBoughtByTheUser: Int,
            val totalUsersInTheRaffle: Int,
            val totalTickets: Int
        ) : RaffleDM()

        class LostTheRaffleNoOneBoughtTickets(
            override val userId: Long,
            override val raffleType: RaffleType,
        ) : RaffleDM()
    }
}