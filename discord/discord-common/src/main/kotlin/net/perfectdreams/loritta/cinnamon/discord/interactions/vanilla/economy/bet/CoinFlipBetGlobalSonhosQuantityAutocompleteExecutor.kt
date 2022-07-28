package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet

import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.CinnamonAutocompleteHandler
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.BetCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import kotlin.time.Duration.Companion.minutes

class CoinFlipBetGlobalSonhosQuantityAutocompleteExecutor(loritta: LorittaCinnamon) : CinnamonAutocompleteHandler<String>(loritta) {
    override suspend fun handle(context: AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, String> {
        val currentInput = focusedOption.value

        val trueNumber = NumberUtils.convertShortenedNumberToLong(
            context.i18nContext,
            currentInput
        )

        val trueNumberAsString = trueNumber.toString()

        val matchedChoices = mutableSetOf<Long>()

        for (quantity in CoinFlipBetGlobalExecutor.QUANTITIES) {
            if (focusedOption.value.isEmpty() || quantity.toString().startsWith(trueNumberAsString)) {
                matchedChoices.add(quantity)
            }
        }

        val discordChoices = mutableMapOf<String, String>()

        val matchmakingStats = loritta.services.bets.getUserCoinFlipBetGlobalMatchmakingStats(
            UserId(context.sender.id.value),
            matchedChoices.toList(),
            Clock.System.now().minus(5.minutes)
        )

        for (choice in matchedChoices) {
            val mmStat = matchmakingStats[choice]

            discordChoices[
                    buildString {
                        if (mmStat == null) {
                            if (choice == 0L) {
                                append(
                                    context.i18nContext.get(
                                        BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.JustForFun
                                    )
                                )
                            } else {
                                append(
                                    context.i18nContext.get(
                                        BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.MatchmakingSonhos(
                                            choice
                                        )
                                    )
                                )
                            }
                        } else {
                            if (mmStat.userPresentInMatchmakingQueue) {
                                append(
                                    context.i18nContext.get(
                                        BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.QuitMatchmakingQueue(
                                            choice
                                        )
                                    )
                                )
                            } else {
                                if (choice == 0L) {
                                    append(
                                        context.i18nContext.get(
                                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.JustForFun
                                        )
                                    )
                                } else {
                                    append(
                                        context.i18nContext.get(
                                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.MatchmakingSonhos(
                                                choice
                                            )
                                        )
                                    )
                                }

                                val averageTimeOnQueue = mmStat.averageTimeOnQueue
                                if (averageTimeOnQueue != null) {
                                    append(" (${
                                        context.i18nContext.get(
                                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.AverageTimeInSeconds(
                                                averageTimeOnQueue.toMillis().toDouble() / 1_000
                                            )
                                        )
                                    })")
                                }
                                append(" ")
                                append("[")
                                if (mmStat.playersPresentInMatchmakingQueue) {
                                    append(
                                        context.i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.PlayersInMatchmakingQueue)
                                    )
                                    append(" | ")
                                }
                                append(
                                    context.i18nContext.get(
                                        BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Choice.RecentMatches(
                                            mmStat.recentMatches
                                        )
                                    )
                                )
                                append("]")
                            }
                        }
                    }
            ] = if (mmStat?.userPresentInMatchmakingQueue == true)
                "q$choice"
            else
                choice.toString()
        }

        return discordChoices
    }
}