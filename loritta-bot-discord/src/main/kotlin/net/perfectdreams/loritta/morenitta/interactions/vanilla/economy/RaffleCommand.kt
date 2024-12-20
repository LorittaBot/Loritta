package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.RaffleTickets
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.UserAskedRaffleNotifications
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.convertToUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.serializable.RaffleStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class RaffleCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Raffle
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("59a50ba4-e0aa-4cd6-bf72-8ee048e78001")) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(I18N_PREFIX.Status.Label, I18N_PREFIX.Status.Description, UUID.fromString("9b2bcfba-eec7-4cd3-8187-4928a39f56b2")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("loraffle")
                add("rifa")
                add("raffle")
                add("lorifa")
            }

            executor = RaffleStatusExecutor()
        }

        subcommand(I18N_PREFIX.Buy.Label, I18N_PREFIX.Buy.Description, UUID.fromString("a6fcf9d8-7feb-4eda-a613-af72614a9bf4")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                listOf("buy", "comprar").forEach {
                    add("loraffle $it")
                    add("lorifa $it")
                    // We don't need these as alternate names because InteraKTions Unleashed will automatically pick up these aliases
                    // add("rifa $it")
                    // add("raffle $it")
                }
            }

            executor = RaffleBuyExecutor()
        }
    }

    inner class RaffleStatusExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val raffleType = RaffleType.valueOf(args[options.raffleType])

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
                KotlinLogging.logger {}.info { "ServerInfoCommand#retrieveUserInfoById - UserId: $lastWinnerId" }
                loritta.lorittaShards.retrieveUserInfoById(lastWinnerId.toLong())
            } else {
                null
            }

            val nameAndDiscriminator = if (lastWinner != null) {
                if (lastWinner.globalName != null && MiscUtils.hasInvite(lastWinner.globalName))
                    "¯\\_(ツ)_/¯ (`${lastWinner.id}`)"
                else
                    convertToUserNameCodeBlockPreviewTag(
                        lastWinner.id,
                        lastWinner.name,
                        lastWinner.globalName,
                        lastWinner.discriminator,
                        stripCodeMarksFromInput = true,
                        stripLinksFromInput = true
                    )
            } else {
                "\uD83E\uDD37 (`$lastWinnerId`)"
            }

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

                            val totalTickets = participants.sumOf { it[raffleTicketsCount] }

                            // Sort by order
                            for (participant in participants.sortedByDescending {
                                it[raffleTicketsCount]
                            }) {
                                val participantId = participant[RaffleTickets.userId]
                                val ticketCount = participant[raffleTicketsCount]
                                KotlinLogging.logger {}.info { "RaffleCommand#retrieveUserInfoById - UserId: $participantId" }
                                val userInfo = loritta.lorittaShards.retrieveUserInfoById(participantId)
                                val hasInviteOnName = userInfo?.name?.let { DiscordInviteUtils.hasInvite(it) }
                                if (userInfo != null && hasInviteOnName == false) {
                                    styled(
                                        context.i18nContext.get(
                                            I18N_PREFIX.Status.Participants.ParticipantEntry(
                                                convertToUserNameCodeBlockPreviewTag(
                                                    userInfo.id,
                                                    userInfo.name,
                                                    userInfo.globalName,
                                                    userInfo.discriminator,
                                                    stripCodeMarksFromInput = true,
                                                    stripLinksFromInput = true
                                                ),
                                                ticketCount,
                                                ticketCount / totalTickets.toDouble()
                                            )
                                        )
                                    )
                                } else {
                                    styled(context.i18nContext.get(I18N_PREFIX.Status.Participants.ParticipantUnknownEntry(participantId.toString(), ticketCount, ticketCount / totalTickets.toDouble())))
                                }
                            }
                        }
                    } else {
                        context.reply(true) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Status.Participants.NoOneIsParticipatingRightNow(loritta.commandMentions.raffleBuy)),
                                net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriLurk
                            )

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
                            context.i18nContext.get(I18N_PREFIX.Status.LastWinnerTaxed(nameAndDiscriminator, lastWinnerPrize, lastWinnerPrizeAfterTax)),
                            "\uD83D\uDE0E",
                        )
                    } else {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Status.LastWinner(nameAndDiscriminator, lastWinnerPrize)),
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

                styled(
                    context.i18nContext.get(I18N_PREFIX.Status.YouCanBuyTickets(raffleType.maxTicketsByUserPerRound, context.i18nContext.get(raffleType.title))),
                    prefix = net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHm,
                )

                actionRow(notifyMeButton, viewParticipants)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            // I think that this should NEVER be null... well, I hope so
            val declarationPath = loritta.interactionsListener.manager.findDeclarationPath(context.commandDeclaration)

            val fullLabel = buildString {
                declarationPath.forEach {
                    when (it) {
                        is SlashCommandDeclaration -> append(context.i18nContext.get(it.name))
                        is SlashCommandGroupDeclaration -> append(context.i18nContext.get(it.name))
                    }
                    this.append(" ")
                }
            }.trim()

            val raffleTypeAsString = args.getOrNull(0)
            val raffleType = raffleTypeAsString?.let {
                RaffleType.values().firstOrNull { context.i18nContext.get(it.shortName).normalize() == raffleTypeAsString.normalize() }
            }

            if (raffleType == null) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Status.YouNeedToSelectWhatRaffleTypeYouWant),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSleeping
                    )

                    for (availableRaffleType in RaffleType.values()) {
                        styled("**${context.i18nContext.get(availableRaffleType.title)}:** `${context.config.commandPrefix}$fullLabel ${context.i18nContext.get(availableRaffleType.shortName)}`")
                    }
                }
                return null
            }

            return mapOf(
                options.raffleType to raffleType.name, // We need to use the raffle type name, not the raffle type reference... Yes, it is kinda "ewww"
            )
        }
    }

    inner class RaffleBuyExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val raffleType = RaffleType.valueOf(args[options.raffleType])

            if (SonhosUtils.checkIfEconomyIsDisabled(context))
                return

            val loritta = context.loritta

            context.deferChannelMessage(false)

            val shard = loritta.config.loritta.clusters.instances.first { it.id == 1 }

            val quantity = args[options.quantity].coerceAtLeast(1)

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

            val status = BuyRaffleTicketStatus.valueOf(json["status"].string)

            if (status == BuyRaffleTicketStatus.THRESHOLD_EXCEEDED) {
                context.reply(false) {
                    styled(
                        "Você já tem tickets demais! Guarde um pouco do seu dinheiro para a próxima rodada!",
                        Constants.ERROR
                    )
                }
                return
            }

            if (status == BuyRaffleTicketStatus.TOO_MANY_TICKETS) {
                context.reply(false) {
                    styled(
                        "Você não pode apostar tantos tickets assim! Você pode apostar, no máximo, mais ${raffleType.maxTicketsByUserPerRound - json["ticketCount"].int} tickets!",
                        Constants.ERROR
                    )
                }
                return
            }

            if (status == BuyRaffleTicketStatus.NOT_ENOUGH_MONEY) {
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

            if (status == BuyRaffleTicketStatus.STALE_RAFFLE_DATA) {
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
                    context.i18nContext.get(I18N_PREFIX.Buy.YouBoughtAnTicket(quantity, quantity * raffleType.ticketPrice, context.i18nContext.get(raffleType.title), loritta.commandMentions.raffleStatus)),
                    net.perfectdreams.loritta.cinnamon.emotes.Emotes.Ticket
                )

                styled(
                    context.i18nContext.get(I18N_PREFIX.Buy.WantMoreChances(loritta.commandMentions.raffleBuy)),
                    net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriKiss
                )
            }
            return
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            // I think that this should NEVER be null... well, I hope so
            val declarationPath = loritta.interactionsListener.manager.findDeclarationPath(context.commandDeclaration)

            val fullLabel = buildString {
                declarationPath.forEach {
                    when (it) {
                        is SlashCommandDeclaration -> append(context.i18nContext.get(it.name))
                        is SlashCommandGroupDeclaration -> append(context.i18nContext.get(it.name))
                    }
                    this.append(" ")
                }
            }.trim()

            val raffleTypeAsString = args.getOrNull(0)
            val raffleType = raffleTypeAsString?.let {
                RaffleType.values().firstOrNull { context.i18nContext.get(it.shortName).normalize().lowercase() == raffleTypeAsString.normalize().lowercase() }
            }

            if (raffleType == null) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Buy.YouNeedToSelectWhatRaffleTypeYouWant),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSleeping
                    )

                    for (availableRaffleType in RaffleType.values()) {
                        styled("**${context.i18nContext.get(availableRaffleType.title)}:** `${context.config.commandPrefix}$fullLabel ${context.i18nContext.get(availableRaffleType.shortName).lowercase()}`")
                    }
                }
                return null
            }

            val quantityAsString = args.getOrNull(1)
            if (quantityAsString == null) {
                context.explain()
                return null
            }
            val quantity = NumberUtils.convertShortenedNumberToLong(quantityAsString)
            if (quantity == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.raffleType to raffleType.name, // We need to use the raffle type name, not the raffle type reference... Yes, it is kinda "ewww"
                options.quantity to quantity
            )
        }
    }

    enum class BuyRaffleTicketStatus {
        SUCCESS,
        THRESHOLD_EXCEEDED,
        TOO_MANY_TICKETS,
        NOT_ENOUGH_MONEY,
        STALE_RAFFLE_DATA
    }
}