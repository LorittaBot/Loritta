package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.pudding.services.BovespaBrokerService
import kotlin.math.abs

class BrokerSellStockExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(BrokerSellStockExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val ticker = string("ticker", BrokerCommand.I18N_PREFIX.Sell.Options.Ticker.Text)
                .also {
                    LorittaBovespaBrokerUtils.trackedTickerCodes.toList().sortedBy { it.first }.forEach { (tickerId, tickerTitle) ->
                        it.choice(tickerId.lowercase(), "$tickerTitle ($tickerId)")
                    }
                }
                .register()

            val quantity = optionalString("quantity", BrokerCommand.I18N_PREFIX.Sell.Options.Quantity.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val tickerId = args[Options.ticker].uppercase()
        val quantityAsString = args[Options.quantity] ?: "1"

        // This should *never* happen because the values are validated on Discord side BUT who knows
        if (tickerId !in LorittaBovespaBrokerUtils.validStocksCodes)
            context.failEphemerally(context.i18nContext.get(BrokerCommand.I18N_PREFIX.ThatIsNotAnValidStockTicker))

        val quantity = if (quantityAsString == "all") {
            context.loritta.services.bovespaBroker.getUserBoughtStocks(context.user.id.value.toLong())
                .firstOrNull { it.ticker == tickerId }
                ?.count ?: context.failEphemerally(
                context.i18nContext.get(
                    BrokerCommand.I18N_PREFIX.Sell.YouDontHaveAnySharesInThatTicker(tickerId)
                )
            )
        } else {
            NumberUtils.convertShortenedNumberToLong(context.i18nContext, quantityAsString) ?: context.failEphemerally(
                context.i18nContext.get(
                    I18nKeysData.Commands.InvalidNumber(quantityAsString)
                )
            )
        }

        val (_, soldQuantity, earnings, profit) = try {
            context.loritta.services.bovespaBroker.sellStockShares(
                context.user.id.value.toLong(),
                tickerId,
                quantity
            )
        } catch (e: BovespaBrokerService.TransactionActionWithLessThanOneShareException) {
            context.failEphemerally(
                context.i18nContext.get(
                    when (quantity) {
                        0L -> BrokerCommand.I18N_PREFIX.Sell.TryingToSellZeroShares
                        else -> BrokerCommand.I18N_PREFIX.Sell.TryingToSellLessThanZeroShares
                    }
                )
            )
        } catch (e: BovespaBrokerService.StaleTickerDataException) {
            context.failEphemerally(context.i18nContext.get(BrokerCommand.I18N_PREFIX.StaleTickerData))
        } catch (e: BovespaBrokerService.OutOfSessionException) {
            context.failEphemerally(
                context.i18nContext.get(
                    BrokerCommand.I18N_PREFIX.StockMarketClosed(
                        LorittaBovespaBrokerUtils.TIME_OPEN_DISCORD_TIMESTAMP,
                        LorittaBovespaBrokerUtils.TIME_CLOSING_DISCORD_TIMESTAMP
                    )
                )
            )
        } catch (e: BovespaBrokerService.NotEnoughSharesException) {
            context.failEphemerally(
                context.i18nContext.get(
                    BrokerCommand.I18N_PREFIX.Sell.YouDontHaveEnoughStocks(
                        e.currentBoughtSharesCount,
                        tickerId
                    )
                )
            )
        }

        val isNeutralProfit = profit == 0L
        val isPositiveProfit = profit > 0L
        val isNegativeProfit = !isNeutralProfit && !isPositiveProfit

        context.sendEphemeralReply(
            context.i18nContext.get(
                BrokerCommand.I18N_PREFIX.Sell.SuccessfullySold(
                    soldQuantity,
                    tickerId,
                    when {
                        isNeutralProfit -> {
                            context.i18nContext.get(
                                BrokerCommand.I18N_PREFIX.Sell.SuccessfullySoldNeutral
                            )
                        }
                        isPositiveProfit -> {
                            context.i18nContext.get(
                                BrokerCommand.I18N_PREFIX.Sell.SuccessfullySoldProfit(
                                    abs(earnings),
                                    abs(profit)
                                )
                            )
                        }
                        else -> {
                            context.i18nContext.get(
                                BrokerCommand.I18N_PREFIX.Sell.SuccessfullySoldLoss(
                                    abs(earnings),
                                    abs(profit)
                                )
                            )
                        }
                    }
                )
            ),
            when {
                profit == 0L -> Emotes.LoriShrug
                profit > 0L -> Emotes.LoriRich
                else -> Emotes.LoriSob
            }
        )

        if (isPositiveProfit)
            context.giveAchievement(AchievementType.STONKS)
        if (isNegativeProfit)
            context.giveAchievement(AchievementType.NOT_STONKS)
    }
}