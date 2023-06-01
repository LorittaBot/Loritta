package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import dev.minn.jda.ktx.messages.MessageEdit
import dev.minn.jda.ktx.messages.editMessage
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.UserAskedRaffleNotifications
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.LoraffleCommand
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.RaffleStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class RaffleCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Raffle

        // Compatibility for the old loraffle message command
        suspend fun executeStatusCompat(context: CommandContextCompat, raffleType: RaffleType) {
            val loritta = context.loritta

            val shard = loritta.config.loritta.clusters.instances.first { it.id == 1 }

            val body = loritta.httpWithoutTimeout.get("${shard.getUrl(loritta)}/api/v1/loritta/raffle?type=${raffleType.name}") {
                userAgent(loritta.lorittaCluster.getUserAgent(loritta))
                header("Authorization", loritta.lorittaInternalApiKey.name)
            }.bodyAsText()

            val raffleStatus = Json.decodeFromString<RaffleStatus>(body)

            val lastWinnerId = raffleStatus.lastWinnerId
            val currentTickets = raffleStatus.currentTickets
            val usersParticipating = raffleStatus.usersParticipating
            val endsAtInSeconds = raffleStatus.endsAt / 1000
            val lastWinnerPrize = raffleStatus.lastWinnerPrize
            val lastWinnerPrizeAfterTax = raffleStatus.lastWinnerPrizeAfterTax
            val raffleId = raffleStatus.raffleId

            val lastWinner = if (lastWinnerId != null) {
                loritta.lorittaShards.retrieveUserInfoById(lastWinnerId.toLong())
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

            val viewParticipants = loritta.interactivityManager
                .button(
                    ButtonStyle.SECONDARY,
                    context.i18nContext.get(I18N_PREFIX.Status.Participants.Label),
                    {
                        loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriCard
                    }
                ) { context ->
                    val hook = context.deferChannelMessage(true)

                    val raffleTicketsCount = RaffleTickets.userId.count()
                    val participants = loritta.transaction {
                        RaffleTickets.slice(RaffleTickets.userId, raffleTicketsCount).select {
                            RaffleTickets.raffle eq raffleId
                        }.groupBy(RaffleTickets.userId)
                            .toList()
                    }

                    if (participants.isNotEmpty()) {
                        context.chunkedReply(true) {
                            content = "# ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriCard} ${context.i18nContext.get(I18N_PREFIX.Status.Participants.RaffleParticipants)}\n"

                            // Sort by order
                            for (participant in participants.sortedByDescending {
                                it[raffleTicketsCount]
                            }) {
                                val participantId = participant[RaffleTickets.userId]
                                val ticketCount = participant[raffleTicketsCount]
                                val userInfo = loritta.lorittaShards.retrieveUserInfoById(participantId)
                                if (userInfo != null) {
                                    styled(context.i18nContext.get(I18N_PREFIX.Status.Participants.ParticipantEntry(userInfo.name + "#" + userInfo.discriminator, participantId.toString(), ticketCount)))
                                } else {
                                    styled(context.i18nContext.get(I18N_PREFIX.Status.Participants.ParticipantUnknownEntry(participantId.toString(), ticketCount)))
                                }
                            }
                        }
                    } else {
                        context.reply(true) {
                            content = "Ninguém está participando da rifa..."
                        }
                    }
                }

            val notifyMeButton = loritta.interactivityManager
                .button(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.Status.NotifyMe.Label),
                    {
                        loriEmoji = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHi
                    }
                ) { context ->
                    val hook = context.deferChannelMessage(true)

                    val addedNotification = loritta.transaction {
                        val alreadyHasNotification = UserAskedRaffleNotifications.select {
                            UserAskedRaffleNotifications.userId eq context.user.idLong and (UserAskedRaffleNotifications.raffle eq raffleId)
                        }.count() != 0L

                        if (alreadyHasNotification) {
                            UserAskedRaffleNotifications.deleteWhere {
                                UserAskedRaffleNotifications.userId eq context.user.idLong and (UserAskedRaffleNotifications.raffle eq raffleId)
                            }

                            return@transaction false
                        } else {
                            UserAskedRaffleNotifications.insert {
                                it[UserAskedRaffleNotifications.raffle] = raffleId
                                it[UserAskedRaffleNotifications.userId] = context.user.idLong
                            }

                            return@transaction true
                        }
                    }

                    context.reply(true) {
                        if (addedNotification) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Status.NotifyMe.NotifyAdded),
                                net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHi
                            )
                        } else {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Status.NotifyMe.NotifyRemoved),
                                net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSleeping
                            )
                        }
                    }
                }

            context.reply(false) {
                styled(
                    "**Lorifa - ${context.i18nContext.get(raffleType.title)}**",
                    "<:loritta:331179879582269451>"
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.Status.CurrentPrize(currentTickets * raffleType.ticketPrice)),
                    "<:starstruck:540988091117076481>",
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.Status.BoughtTickets(currentTickets)),
                    "\uD83C\uDFAB",
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.Status.UsersParticipating(usersParticipating)),
                    "\uD83D\uDC65",
                )

                if (lastWinnerId != null && lastWinnerPrize != null) {
                    if (lastWinnerPrizeAfterTax != null && lastWinnerPrizeAfterTax != lastWinnerPrize) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Status.LastWinnerTaxed("$nameAndDiscriminator (${lastWinner?.id})", lastWinnerPrize, lastWinnerPrizeAfterTax)),
                            "\uD83D\uDE0E",
                        )
                    } else {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Status.LastWinner("$nameAndDiscriminator (${lastWinner?.id})", lastWinnerPrize)),
                            "\uD83D\uDE0E",
                        )
                    }
                }

                styled(
                    context.i18nContext.get(I18N_PREFIX.Status.ResultsIn("<t:${endsAtInSeconds}:f> (<t:${endsAtInSeconds}:R>)")),
                    "\uD83D\uDD52",
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.Status.BuyAnTicketFor(raffleType.ticketPrice, loritta.commandMentions.raffleBuy)),
                    prefix = "\uD83D\uDCB5",
                )

                actionRow(notifyMeButton, viewParticipants)
            }
        }

        // Compatibility for the old loraffle message command
        suspend fun executeBuyCompat(context: CommandContextCompat, raffleType: RaffleType, quantity: Long) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context))
                return

            val loritta = context.loritta

            context.deferChannelMessage(false)

            val shard = loritta.config.loritta.clusters.instances.first { it.id == 1 }

            val quantity = quantity.coerceAtLeast(1)

            val dailyReward = AccountUtils.getUserTodayDailyReward(loritta, loritta.getOrCreateLorittaProfile(context.user.idLong))

            if (dailyReward == null) { // Nós apenas queremos permitir que a pessoa aposte na rifa caso já tenha pegado sonhos alguma vez hoje
                context.reply(false) {
                    styled(
                        context.locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", context.config.commandPrefix],
                        Constants.ERROR
                    )
                }
                return
            }

            if (quantity > raffleType.maxTicketsByUserPerRound) {
                context.reply(false) {
                    styled(
                        "Você só pode apostar no máximo ${raffleType.maxTicketsByUserPerRound} tickets por rodada!",
                        Constants.ERROR
                    )
                }
                return
            }

            val body = loritta.httpWithoutTimeout.post("${shard.getUrl(loritta)}/api/v1/loritta/raffle") {
                userAgent(loritta.lorittaCluster.getUserAgent(loritta))
                header("Authorization", loritta.lorittaInternalApiKey.name)

                setBody(
                    TextContent(
                        Json.encodeToString(
                            buildJsonObject {
                                put("userId", context.user.idLong)
                                put("quantity", quantity)
                                put("localeId", context.config.localeId)
                                put("invokedAt", System.currentTimeMillis())
                                put("type", raffleType.name)
                            }
                        ),
                        ContentType.Application.Json
                    )
                )
            }.bodyAsText()

            val json = JsonParser.parseString(body)

            val status = LoraffleCommand.BuyRaffleTicketStatus.valueOf(json["status"].string)

            if (status == LoraffleCommand.BuyRaffleTicketStatus.THRESHOLD_EXCEEDED) {
                context.reply(false) {
                    styled(
                        "Você já tem tickets demais! Guarde um pouco do seu dinheiro para a próxima rodada!",
                        Constants.ERROR
                    )
                }
                return
            }

            if (status == LoraffleCommand.BuyRaffleTicketStatus.TOO_MANY_TICKETS) {
                context.reply(false) {
                    styled(
                        "Você não pode apostar tantos tickets assim! Você pode apostar, no máximo, mais ${raffleType.maxTicketsByUserPerRound - json["ticketCount"].int} tickets!",
                        Constants.ERROR
                    )
                }
                return
            }

            if (status == LoraffleCommand.BuyRaffleTicketStatus.NOT_ENOUGH_MONEY) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.raffle.notEnoughMoney", json["canOnlyPay"].int, quantity, if (quantity == 1L) "" else "s"],
                        Constants.ERROR
                    )

                    styled(
                        context.i18nContext.get(
                            GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                "https://loritta.website/", // Hardcoded, woo
                                "loraffle-legacy",
                                "buy-tickets-not-enough-sonhos"
                            )
                        ),
                        prefix = Emotes.LORI_RICH.asMention
                    )
                }
                return
            }

            if (status == LoraffleCommand.BuyRaffleTicketStatus.STALE_RAFFLE_DATA) {
                context.reply(false) {
                    styled(
                        "O resultado da rifa demorou tanto para sair que já começou uma nova rifa enquanto você comprava!",
                        Constants.ERROR
                    )
                }
                return
            }

            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.Buy.YouBoughtAnTicket(quantity, quantity * raffleType.ticketPrice, loritta.commandMentions.raffleStatus)),
                    net.perfectdreams.loritta.cinnamon.emotes.Emotes.Ticket
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.Buy.WantMoreChances(loritta.commandMentions.raffleBuy)),
                    net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriKiss
                )
            }
            return
        }
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY) {
        subcommand(I18N_PREFIX.Status.Label, I18N_PREFIX.Status.Description) {
            executor = RaffleStatusExecutor()
        }

        subcommand(I18N_PREFIX.Buy.Label, I18N_PREFIX.Buy.Description) {
            executor = RaffleBuyExecutor()
        }
    }

    inner class RaffleStatusExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val raffleType = string(
                "raffle_type",
                I18N_PREFIX.Status.Options.RaffleType.Text
            ) {
                for (raffleType in RaffleType.values()) {
                    choice(raffleType.title, raffleType.name)
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            executeStatusCompat(
                CommandContextCompat.InteractionsCommandContextCompat(context),
                RaffleType.valueOf(args[options.raffleType])
            )
        }
    }

    inner class RaffleBuyExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val raffleType = string(
                "raffle_type",
                I18N_PREFIX.Buy.Options.RaffleType.Text
            ) {
                for (raffleType in RaffleType.values()) {
                    choice(raffleType.title, raffleType.name)
                }
            }

            val quantity = long(
                "quantity",
                I18N_PREFIX.Buy.Options.Quantity.Text
            )
        }

        override val options = Options()

        override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
            executeBuyCompat(
                CommandContextCompat.InteractionsCommandContextCompat(context),
                RaffleType.valueOf(args[options.raffleType]),
                args[options.quantity]
            )
        }
    }
}