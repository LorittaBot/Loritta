package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.pudding.services.BovespaBrokerService

class BrokerBuyStockExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(BrokerBuyStockExecutor::class) {
        object Options : CommandOptions() {
            val ticker = string("ticker", TodoFixThisData)
                .also {
                    LorittaBovespaBrokerUtils.trackedTickerCodes.toList().sortedBy { it.first }.forEach { (tickerId, tickerTitle) ->
                        it.choice(tickerId.lowercase(), "$tickerTitle ($tickerId)")
                    }
                }
                .register()

            val quantity = string("quantity", TodoFixThisData)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally()

        val tickerId = args[Options.ticker].uppercase()
        val quantityAsString = args[Options.quantity]

        // This should *never* happen because the values are validated on Discord side BUT who knows
        if (tickerId !in LorittaBovespaBrokerUtils.validStocksCodes)
            context.failEphemerally(context.i18nContext.get(BrokerCommand.I18N_PREFIX.ThatIsNotAnValidStockTicker))

        val quantity = NumberUtils.convertShortenedNumberToLong(context.i18nContext, quantityAsString) ?: context.failEphemerally(
            context.i18nContext.get(
                I18nKeysData.Commands.InvalidNumber(quantityAsString)
            )
        )

        val (_, boughtQuantity, value) = try {
            context.loritta.services.bovespaBroker.buyStockShares(
                context.user.id.value.toLong(),
                tickerId,
                quantity
            )
        } catch (e: BovespaBrokerService.TransactionActionWithLessThanOneShareException) {
            context.failEphemerally(
                context.i18nContext.get(
                    when (quantity) {
                        0L -> BrokerCommand.I18N_PREFIX.Buy.TryingToBuyZeroShares
                        else -> BrokerCommand.I18N_PREFIX.Buy.TryingToBuyLessThanZeroShares
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
        } catch (e: BovespaBrokerService.NotEnoughSonhosException) {
            context.failEphemerally(context.i18nContext.get(BrokerCommand.I18N_PREFIX.Buy.YouDontHaveEnoughSonhos))
        } catch (e: BovespaBrokerService.TooManySharesException) {
            context.failEphemerally(
                context.i18nContext.get(BrokerCommand.I18N_PREFIX.Buy.TooManyStocks(LorittaBovespaBrokerUtils.MAX_STOCKS_PER_USER))
            )
        }

        context.sendEphemeralReply(
            context.i18nContext.get(
                BrokerCommand.I18N_PREFIX.Buy.SuccessfullyBought(
                    stockCount = boughtQuantity,
                    ticker = tickerId,
                    price = value
                )
            ),
            Emotes.LoriRich
        )
    }
}