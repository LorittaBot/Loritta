package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.lottery

import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.rpc.payloads.BuyLotteryTicketRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.BuyLotteryTicketResponse
import net.perfectdreams.loritta.morenitta.rpc.payloads.ViewLotteryStatusRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.ViewLotteryStatusResponse
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import java.util.*

class LotteryCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Lottery
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("53568a01-5c50-4bbf-9e9b-2931dd4dd396")) {
        this.enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)

        subcommand(I18N_PREFIX.Buy.Label, I18N_PREFIX.Buy.Description, UUID.fromString("85abe25a-c227-4f7b-ae15-97d878a04397")) {
            executor = LotteryBuyTicketExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Status.Label, I18N_PREFIX.Status.Description, UUID.fromString("53148038-7a52-4a08-88b4-907669449d1b")) {
            executor = LotteryStatusExecutor(loritta)
        }
    }

    class LotteryBuyTicketExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val numbers = string("numbers", I18N_PREFIX.Buy.Options.Numbers.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val numbersAsString = args[options.numbers]

            val numbers = numbersAsString
                .replace(",", "")
                .split(" ")
                .map { it.toIntOrNull() }

            if (numbers.any { it == null }) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Buy.InvalidInput),
                        Emotes.Error
                    )
                }
                return
            }

            val numbersNotNull = numbers.filterNotNull()

            val response = LorittaRPC.BuyLotteryTicket.execute(
                loritta,
                loritta.lorittaMainCluster,
                BuyLotteryTicketRequest(
                    context.user.idLong,
                    numbersNotNull
                )
            )

            when (response) {
                is BuyLotteryTicketResponse.Success -> {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Buy.YouBoughtAnTicket(response.ticketPrice, LotteryUtils.formatTicketNumbers(numbersNotNull), loritta.commandMentions.lotteryStatus)),
                            Emotes.Ticket
                        )

                        styled(
                            context.i18nContext.get(I18N_PREFIX.Buy.WantMoreChances(loritta.commandMentions.lotteryBuy)),
                            Emotes.LoriKiss
                        )
                    }
                }
                BuyLotteryTicketResponse.ThereIsntActiveLottery -> {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Buy.NoActiveLottery),
                            Emotes.LoriSob
                        )
                    }
                }

                BuyLotteryTicketResponse.AlreadyBettedWithTheseNumbers -> {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Buy.YouHaveAlreadyBettedWithTheseNumbers),
                            Emotes.Error
                        )
                    }
                }

                is BuyLotteryTicketResponse.IncorrectNumberCount -> {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Buy.YouNeedToBetXNumbers(response.requiredCount)),
                            Emotes.Error
                        )
                    }
                }

                BuyLotteryTicketResponse.RepeatedNumbers -> {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Buy.YouCannotBetRepeatedNumbers),
                            Emotes.Error
                        )
                    }
                }

                is BuyLotteryTicketResponse.InvalidNumbers -> {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Buy.InvalidNumber(1, response.tableTotalNumbers)),
                            Emotes.Error
                        )
                    }
                }

                is BuyLotteryTicketResponse.NotEnoughSonhos -> {
                    context.reply(true) {
                        this.styled(
                            context.locale["commands.command.flipcoinbet.notEnoughMoneySelf"],
                            Emotes.Error
                        )

                        this.styled(
                            context.i18nContext.get(
                                GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                    loritta.config.loritta.dashboard.url,
                                    "lottery",
                                    "buy-not-enough-sonhos"
                                )
                            ),
                            Emotes.LoriRich.asMention
                        )
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.numbers to args.joinToString(" ")
            )
        }
    }

    class LotteryStatusExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val response = LorittaRPC.ViewLotteryStats.execute(
                loritta,
                loritta.lorittaMainCluster,
                ViewLotteryStatusRequest(context.user.idLong)
            )

            when (response) {
                is ViewLotteryStatusResponse.Success -> {
                    var currentPrize = (response.ticketPrice * response.totalTickets)
                    if (response.houseSponsorship != null)
                        currentPrize += response.houseSponsorship

                    context.reply(false) {
                        styled(
                            "**Loteritta**",
                            "<:loritta:331179879582269451>"
                        )

                        if (response.houseSponsorship != null) {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Status.CurrentPrizeWithHouseSponsorship(currentPrize, response.houseSponsorship)),
                                "\uD83E\uDD29",
                            )
                        } else {
                            styled(
                                context.i18nContext.get(I18N_PREFIX.Status.CurrentPrize(currentPrize)),
                                "\uD83E\uDD29",
                            )
                        }

                        styled(
                            context.i18nContext.get(I18N_PREFIX.Status.BoughtTickets(response.totalTickets)),
                            Emotes.Ticket
                        )

                        styled(
                            context.i18nContext.get(I18N_PREFIX.Status.UserBoughtTickets(response.howManyTicketsYouBought)),
                        )

                        styled(
                            context.i18nContext.get(I18N_PREFIX.Status.UsersParticipating(response.usersParticipating)),
                            "\uD83D\uDC65",
                        )

                        val results = response.results
                        if (results == null) {
                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.Status.ResultsIn(
                                        DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(response.endsAt.toJavaInstant())
                                    )
                                ),
                                "\uD83D\uDD52",
                            )

                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.Status.BuyAnTicketFor(response.ticketPrice, loritta.commandMentions.lotteryBuy, 1, response.tableTotalNumbers),
                                ),
                                Emotes.DollarBill
                            )
                        } else {
                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.Status.WinningNumbers(
                                        LotteryUtils.formatTicketNumbers(results.winningNumbers)
                                    )
                                )
                            )

                            val hits = results.hits

                            if (hits != null) {
                                styled(
                                    context.i18nContext.get(
                                        I18N_PREFIX.Status.Hits(hits)
                                    )
                                )
                            }
                        }
                    }
                }
                ViewLotteryStatusResponse.ThereIsntActiveLottery -> {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.Status.NoActiveLottery),
                            Emotes.LoriSob
                        )
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }
}