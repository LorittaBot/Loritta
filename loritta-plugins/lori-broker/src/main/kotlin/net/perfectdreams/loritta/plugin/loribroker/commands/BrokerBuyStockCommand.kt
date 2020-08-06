package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.content
import kotlinx.serialization.json.double
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.NumberUtils
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.batchInsert

object BrokerBuyStockCommand : DSLCommandBase {
	override fun command(plugin: LoriBrokerPlugin, loritta: Loritta) = create(
			loritta,
			plugin.aliases.flatMap { listOf("$it buy", "$it comprar") }
	) {
		description { it["commands.economy.brokerBuy.description"] }

		arguments {
			argument(ArgumentType.TEXT) {}
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			val tickerId = this.args.getOrNull(0)
					?.toUpperCase()
					?: explainAndExit()

			if (!plugin.validStocksCodes.any { it == this.args[0] })
				fail(locale["commands.economy.broker.invalidTickerId", locale["commands.economy.brokerBuy.baseExample", serverConfig.commandPrefix]])

			val ticker = plugin.tradingApi
					.getOrRetrieveTicker(tickerId, listOf("lp", "description"))

			if (ticker["current_session"]!!.content != LoriBrokerPlugin.MARKET)
				fail(locale["commands.economy.broker.outOfSession"])

			val mutex = plugin.mutexes.getOrPut(user.idLong, { Mutex() })
			if (mutex.isLocked)
				fail(locale["commands.economy.broker.alreadyExecutingAction"])

			val quantity = this.args.getOrNull(1) ?: "1"

			val number = NumberUtils.convertShortenedNumberToLong(quantity)
					?: fail(locale["commands.invalidNumber", quantity], Emotes.LORI_CRYING.toString())

			if (0 >= number)
				fail(locale["commands.economy.brokerBuy.zeroValue"], Constants.ERROR)

			val selfUserProfile = lorittaUser.profile

			val valueOfStock = plugin.convertReaisToSonhos(ticker["lp"]!!.double)
			val howMuchValue = valueOfStock * number

			if (howMuchValue > selfUserProfile.money)
				fail(locale["commands.economy.brokerBuy.notEnoughMoney"], Constants.ERROR)

			val user = user
			val now = System.currentTimeMillis()

			mutex.withLock {
				loritta.newSuspendedTransaction {
					BoughtStocks.batchInsert(0 until number) {
						this[BoughtStocks.user] = user.idLong
						this[BoughtStocks.ticker] = tickerId
						this[BoughtStocks.price] = valueOfStock
						this[BoughtStocks.boughtAt] = now
					}

					lorittaUser.profile.takeSonhosNested(howMuchValue)
					PaymentUtils.addToTransactionLogNested(
							howMuchValue,
							SonhosPaymentReason.STOCKS,
							givenBy = user.idLong
					)
				}
			}

			reply(
					LorittaReply(
							locale[
									"commands.economy.brokerBuy.successfullyBought",
									number,
									locale["commands.economy.broker.stocks.${if (number == 1L) "one" else "multiple"}"],
									tickerId,
									locale["commands.economy.broker.portfolioExample", serverConfig.commandPrefix]
							],
							Emotes.LORI_RICH
					)
			)
		}
	}
}