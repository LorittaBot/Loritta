package net.perfectdreams.loritta.morenitta.christmas2022event.listeners

import com.github.salomonbrys.kotson.jsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Drops
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.Christmas2022Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.CollectedChristmas2022Points
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.BotVoteSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.Christmas2022SonhosTransactionsLog
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.christmas2022event.LorittaChristmas2022Event
import net.perfectdreams.loritta.morenitta.dao.Payment
import net.perfectdreams.loritta.morenitta.tables.DonationKeys
import net.perfectdreams.loritta.morenitta.tables.Payments
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.WebsiteVoteUtils
import net.perfectdreams.loritta.morenitta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.morenitta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.*
import java.time.Instant

class ReactionListener(val m: LorittaBot) : ListenerAdapter() {
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild)
            return

        if (event.user?.isBot == true)
            return

        val userId = event.userIdLong
        val guild = event.guild
        val emoji = event.reaction.emoji

        if (emoji !is CustomEmoji)
            return

        if (emoji.idLong != LorittaChristmas2022Event.emoji.idLong)
            return

        if (!LorittaChristmas2022Event.isEventActive())
            return

        GlobalScope.launch(m.coroutineDispatcher) {
            val lorittaProfile = m.getLorittaProfile(userId) ?: return@launch

            m.newSuspendedTransaction {
                val isParticipating = Christmas2022Players.select {
                    Christmas2022Players.id eq lorittaProfile.id
                }.count() != 0L

                // Bye
                if (!isParticipating)
                    return@newSuspendedTransaction

                // Does this message have any drop in it?
                val dropData = Christmas2022Drops.select {
                    Christmas2022Drops.messageId eq event.messageIdLong
                }.firstOrNull()

                // Bye²
                if (dropData == null || dropData[Christmas2022Drops.createdAt].isBefore(Instant.now().minusMillis(900_000)))
                    return@newSuspendedTransaction

                val hasGotTheDrop = (CollectedChristmas2022Points innerJoin Christmas2022Drops).select {
                    CollectedChristmas2022Points.user eq event.userIdLong and
                            (Christmas2022Drops.messageId eq event.messageIdLong)
                }.firstOrNull()

                // Bye³
                if (hasGotTheDrop != null)
                    return@newSuspendedTransaction

                // Insert the collected point
                CollectedChristmas2022Points.insert {
                    it[user] = lorittaProfile.id
                    it[message] = dropData[Christmas2022Drops.id]
                    it[points] = 1
                    it[collectedAt] = Instant.now()
                }

                // How many points do they now have?
                val pointsSumColumn = CollectedChristmas2022Points.points.sum()
                val pointsSum = CollectedChristmas2022Points.slice(pointsSumColumn).select {
                    CollectedChristmas2022Points.user eq userId
                }.first()[pointsSumColumn]

                for (reward in LorittaChristmas2022Event.eventRewards) {
                    if (reward.requiredPoints != pointsSum)
                        continue

                    when (reward) {
                        is LorittaChristmas2022Event.EventReward.SonhosReward -> {
                            val transactionLogId = SonhosTransactionsLog.insertAndGetId {
                                it[user] = userId
                                it[timestamp] = Instant.now()
                            }

                            Christmas2022SonhosTransactionsLog.insert {
                                it[timestampLog] = transactionLogId
                                it[sonhos] = reward.sonhos
                                it[gifts] = reward.requiredPoints
                            }

                            Profiles.update({ Profiles.id eq userId }) {
                                with(SqlExpressionBuilder) {
                                    it[money] = money + reward.sonhos
                                }
                            }
                        }
                        is LorittaChristmas2022Event.EventReward.PremiumKeyReward -> {
                            DonationKeys.insert {
                                it[DonationKeys.userId] = event.userIdLong
                                it[value] = 100.0
                                it[expiresAt] = System.currentTimeMillis() + (Constants.DONATION_ACTIVE_MILLIS * 3)
                                it[metadata] = jsonObject("type" to "LorittaChristmas2022Event")
                            }

                            Payment.new {
                                this.createdAt = System.currentTimeMillis()
                                this.discount = 0.0
                                this.paidAt = System.currentTimeMillis()
                                this.expiresAt = System.currentTimeMillis() + (Constants.DONATION_ACTIVE_MILLIS * 3)
                                this.userId = event.userIdLong
                                this.gateway = PaymentGateway.OTHER
                                this.reason = PaymentReason.DONATION
                                this.money = 100.0.toBigDecimal()
                            }
                        }
                        is LorittaChristmas2022Event.EventReward.ProfileDesignReward -> {
                            val internalName = reward.profileName
                            val alreadyHasTheBackground = BackgroundPayments.select { BackgroundPayments.userId eq userId and (BackgroundPayments.background eq internalName) }
                                .count() != 0L

                            if (!alreadyHasTheBackground) {
                                BackgroundPayments.insert {
                                    it[BackgroundPayments.userId] = lorittaProfile.id.value
                                    it[cost] = 0
                                    it[background] = internalName
                                    it[boughtAt] = System.currentTimeMillis()
                                }
                            }
                        }
                        is LorittaChristmas2022Event.EventReward.BadgeReward -> {
                            // noop
                        }
                    }
                }
            }
        }
    }
}