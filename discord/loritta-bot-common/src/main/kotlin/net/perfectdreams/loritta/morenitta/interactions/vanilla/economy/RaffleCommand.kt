package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.EmojiFight
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.LoraffleCommand
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.*

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

            val json = JsonParser.parseString(body)

            val lastWinnerId = json["lastWinnerId"].nullString
                ?.toLongOrNull()
            val currentTickets = json["currentTickets"].int
            val usersParticipating = json["usersParticipating"].int
            val endsAt = json["endsAt"].long
            val endsAtInSeconds = endsAt / 1000
            val lastWinnerPrize = json["lastWinnerPrize"].long

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

                if (lastWinnerId != null) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Status.LastWinner("$nameAndDiscriminator (${lastWinner?.id})", lastWinnerPrize)),
                        "\uD83D\uDE0E",
                    )
                }

                styled(
                    context.i18nContext.get(I18N_PREFIX.Status.ResultsIn("<t:${endsAtInSeconds}:f> (<t:${endsAtInSeconds}:R>)")),
                    "\uD83D\uDD52",
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.Status.BuyAnTicketFor(raffleType.ticketPrice, loritta.commandMentions.raffleBuy)),
                    prefix = "\uD83D\uDCB5",
                )
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