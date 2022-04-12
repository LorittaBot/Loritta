package net.perfectdreams.loritta.cinnamon.platform.commands.economy.broker

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.SonhosUtils.userHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.services.BovespaBrokerService

class BrokerBuyStockExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(BrokerBuyStockExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val ticker = string("ticker",
                I18nKeysData.Innercommands.Innercommand.Innerbroker.Innerbuy.Inneroptions.Innerticker.Text
            )
                .also {
                    LorittaBovespaBrokerUtils.trackedTickerCodes.toList().sortedBy { it.first }.forEach { (tickerId, tickerTitle) ->
                        it.choice(tickerId.lowercase(), "$tickerTitle ($tickerId)")
                    }
                }
                .register()

            val quantity = optionalString("quantity",
                I18nKeysData.Innercommands.Innercommand.Innerbroker.Innerbuy.Inneroptions.Innerquantity.Text
            )
                .autocomplete(BrokerStockQuantityAutocompleteExecutor)
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
            context.failEphemerally(context.i18nContext.get(I18nKeysData.Innercommands.Innercommand.Innerbroker.ThatIsNotAnValidStockTicker))

        val quantity = NumberUtils.convertShortenedNumberToLong(context.i18nContext, quantityAsString) ?: context.failEphemerally(
            context.i18nContext.get(
                I18nKeysData.Innercommands.InvalidNumber(quantityAsString)
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
                        0L -> I18nKeysData.Innercommands.Innercommand.Innerbroker.Innerbuy.TryingToBuyZeroShares
                        else -> I18nKeysData.Innercommands.Innercommand.Innerbroker.Innerbuy.TryingToBuyLessThanZeroShares
                    }
                )
            )
        } catch (e: BovespaBrokerService.StaleTickerDataException) {
            context.failEphemerally(context.i18nContext.get(I18nKeysData.Innercommands.Innercommand.Innerbroker.StaleTickerData))
        } catch (e: BovespaBrokerService.OutOfSessionException) {
            context.failEphemerally(
                context.i18nContext.get(
                    I18nKeysData.Innercommands.Innercommand.Innerbroker.StockMarketClosed(
                        LorittaBovespaBrokerUtils.TIME_OPEN_DISCORD_TIMESTAMP,
                        LorittaBovespaBrokerUtils.TIME_CLOSING_DISCORD_TIMESTAMP
                    )
                )
            )
        } catch (e: BovespaBrokerService.NotEnoughSonhosException) {
            context.failEphemerally {
                styled(
                    context.i18nContext.get(I18nKeysData.Innercommands.Innercommand.Innerbroker.Innerbuy.YouDontHaveEnoughSonhos),
                    Emotes.LoriSob
                )

                userHaventGotDailyTodayOrUpsellSonhosBundles(
                    context.loritta,
                    context.i18nContext,
                    UserId(context.user.id.value),
                    "lori-broker",
                    "buy-shares-not-enough-sonhos"
                )
            }
        } catch (e: BovespaBrokerService.TooManySharesException) {
            context.failEphemerally(
                context.i18nContext.get(
                    I18nKeysData.Innercommands.Innercommand.Innerbroker.Innerbuy.TooManyShares(
                        LorittaBovespaBrokerUtils.MAX_STOCK_SHARES_PER_USER
                    )
                )
            )
        }

        context.sendEphemeralReply(
            context.i18nContext.get(
                I18nKeysData.Innercommands.Innercommand.Innerbroker.Innerbuy.SuccessfullyBought(
                    sharesCount = boughtQuantity,
                    ticker = tickerId,
                    price = value
                )
            ),
            Emotes.LoriRich
        )
    }
}