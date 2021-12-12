package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.pudding.services.BovespaBrokerService

class BrokerBuyStockExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(BrokerBuyStockExecutor::class) {
        object Options : CommandOptions() {
            val ticker = string("ticker", TodoFixThisData)
                .register()

            val quantity = integer("quantity", TodoFixThisData)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally()

        val tickerId = args[Options.ticker]
        val quantity = args[Options.quantity]

        // This should *never* happen because the values are validated on Discord side BUT who knows
        // TODO: Improve this
        if (tickerId !in BrokerInfo.validStocksCodes)
            context.failEphemerally("That is not a valid stock ticker!")

        try {
            context.loritta.services.bovespaBroker.buyStockAsset(
                context.user.id.value.toLong(),
                tickerId,
                quantity
            )
        } catch (e: BovespaBrokerService.OutOfSessionException) {
            context.failEphemerally("A corretora não está funcionando no momento!")
        } catch (e: BovespaBrokerService.NotEnoughSonhosException) {
            context.failEphemerally("Sonhos insuficientes!")
        } catch (e: BovespaBrokerService.TooManyStocksException) {
            context.failEphemerally("Você já tem muitas ações!")
        }

        context.sendEphemeralMessage {
            content = "Prontinho meu chapa tá na mão"
        }
        /* val tickerId = this.args.getOrNull(0)
					?.toUpperCase()
					?: explainAndExit()

			if (!plugin.validStocksCodes.any { it == this.args[0] })
				fail(locale["commands.command.broker.invalidTickerId", locale["commands.command.brokerbuy.baseExample", serverConfig.commandPrefix]])

			val ticker = plugin.tradingApi
					.getOrRetrieveTicker(tickerId, listOf(LoriBrokerPlugin.CURRENT_PRICE_FIELD, "description"))

			if (ticker["current_session"]!!.jsonPrimitive.content != LoriBrokerPlugin.MARKET)
				fail(locale["commands.command.broker.outOfSession"])

			val mutex = plugin.mutexes.getOrPut(user.idLong, { Mutex() })
			if (mutex.isLocked)
				fail(locale["commands.command.broker.alreadyExecutingAction"])

			val quantity = this.args.getOrNull(1) ?: "1"

			val number = NumberUtils.convertShortenedNumberToLong(quantity)
					?: GenericReplies.invalidNumber(this, quantity)

			if (0 >= number)
				fail(locale["commands.command.brokerbuy.zeroValue"], Constants.ERROR)

			val selfUserProfile = lorittaUser.profile

			val valueOfStock = plugin.convertToBuyingPrice(
					plugin.convertReaisToSonhos(ticker[LoriBrokerPlugin.CURRENT_PRICE_FIELD]!!.jsonPrimitive.double)
			)

			val howMuchValue = valueOfStock * number

			if (howMuchValue > selfUserProfile.money)
				fail(locale["commands.command.brokerbuy.notEnoughMoney"], Constants.ERROR)

			val user = user
			val now = System.currentTimeMillis()

			mutex.withLock {
				logger.info { "User ${this.user.idLong} is trying to buy $number $tickerId for $howMuchValue" }
				loritta.newSuspendedTransaction {
					val currentStockCount = BoughtStocks.select {
						BoughtStocks.user eq user.idLong
					}.count()

					if (number + currentStockCount > LoriBrokerPlugin.MAX_STOCKS)
						fail(locale["commands.command.brokerbuy.tooManyStocks", LoriBrokerPlugin.MAX_STOCKS])

					// By using shouldReturnGeneratedValues, the database won't need to synchronize on each insert
					// this increases insert performance A LOT and, because we don't need the IDs, it is very useful to make
					// stocks purchases be VERY fast
					BoughtStocks.batchInsert(0 until number, shouldReturnGeneratedValues = false) {
						this[BoughtStocks.user] = user.idLong
						this[BoughtStocks.ticker] = tickerId
						this[BoughtStocks.price] = valueOfStock
						this[BoughtStocks.boughtAt] = now
					}

					lorittaUser.profile.takeSonhosAndAddToTransactionLogNested(
						howMuchValue,
						SonhosPaymentReason.STOCKS
					)
				}
				logger.info { "User ${this.user.idLong} bought $number $tickerId for $howMuchValue" }
			}

			reply(
					LorittaReply(
							locale[
									"commands.command.brokerbuy.successfullyBought",
									number,
									locale["commands.command.broker.stocks.${if (number == 1L) "one" else "multiple"}"],
									tickerId,
									locale["commands.command.broker.portfolioExample", serverConfig.commandPrefix]
							],
							Emotes.LORI_RICH
					)
			)
			*/
    }
}