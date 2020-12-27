package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.utils.Constants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.utils.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.kotlin.utils.addToStdlib.sumByLong
import kotlin.math.abs

class BrokerSellStockCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases.flatMap { listOf("$it sell", "$it vender") }, CommandCategory.ECONOMY) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun command() = create {
		localizedDescription("commands.economy.brokerSell.description")

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
					.getOrRetrieveTicker(tickerId, listOf(LoriBrokerPlugin.CURRENT_PRICE_FIELD, "description"))

			if (ticker["current_session"]!!.jsonPrimitive.content != LoriBrokerPlugin.MARKET)
				fail(locale["commands.economy.broker.outOfSession"])

			val mutex = plugin.mutexes.getOrPut(user.idLong, { Mutex() })
			if (mutex.isLocked)
				fail(locale["commands.economy.broker.alreadyExecutingAction"])

			var quantity = this.args.getOrNull(1) ?: "1"

			if (this.args.getOrNull(1) == "all") {
				val selfStocks = loritta.newSuspendedTransaction {
					BoughtStocks.select {
						BoughtStocks.user eq user.idLong and (BoughtStocks.ticker eq tickerId)
					}.count()
				}

				quantity = selfStocks.toString()
			}

			val number = NumberUtils.convertShortenedNumberToLong(quantity)
					?: GenericReplies.invalidNumber(this, quantity)

			if (0 >= number)
				fail(locale["commands.economy.brokerSell.zeroValue"], Constants.ERROR)

			mutex.withLock {
				val selfStocks = loritta.newSuspendedTransaction {
					BoughtStocks.select {
						BoughtStocks.user eq user.idLong and (BoughtStocks.ticker eq tickerId)
					}.toList()
				}

				if (number > selfStocks.size)
					fail(locale["commands.economy.brokerSell.notEnoughStocks", tickerId])

				val stocksThatWillBeSold = selfStocks.take(number.toInt())
				val howMuchWillBePaidToTheUser = plugin.convertToSellingPrice(
						plugin.convertReaisToSonhos(ticker[LoriBrokerPlugin.CURRENT_PRICE_FIELD]?.jsonPrimitive?.double!!)
				) * number

				logger.info { "User ${this.user.idLong} is trying to sell $number $tickerId for $howMuchWillBePaidToTheUser" }
				
				val totalEarnings = howMuchWillBePaidToTheUser - stocksThatWillBeSold.sumByLong { it[BoughtStocks.price] }

				loritta.newSuspendedTransaction {
					// To avoid selling "phantom" stocks, we need to check if the user has enough stocks inside the transaction
					// If the stock value changed, then it means that the user bought (or sold!) stocks while this transaction was being executed
					val canBeExecuted = BoughtStocks.select {
						BoughtStocks.user eq user.idLong and (BoughtStocks.ticker eq tickerId)
					}.count() == selfStocks.size.toLong()

					if (!canBeExecuted)
						fail(locale["commands.economy.brokerSell.boughtStocksWhileSelling"])

					BoughtStocks.deleteWhere {
						BoughtStocks.id inList stocksThatWillBeSold.map { it[BoughtStocks.id] }
					}

					lorittaUser.profile.addSonhosNested(howMuchWillBePaidToTheUser)
					PaymentUtils.addToTransactionLogNested(
							howMuchWillBePaidToTheUser,
							SonhosPaymentReason.STOCKS,
							receivedBy = user.idLong
					)
				}

				logger.info { "User ${this.user.idLong} sold $number $tickerId for $howMuchWillBePaidToTheUser" }

				reply(
						LorittaReply(
								locale[
										"commands.economy.brokerSell.successfullySold",
										stocksThatWillBeSold.size,
										locale["commands.economy.broker.stocks.${if (stocksThatWillBeSold.size == 1) "one" else "multiple"}"],
										tickerId,
										when {
											totalEarnings == 0L -> {
												locale[
														"commands.economy.brokerSell.successfullySoldNeutral"
												]
											}
											totalEarnings > 0L -> {
												locale[
														"commands.economy.brokerSell.successfullySoldProfit",
														abs(howMuchWillBePaidToTheUser),
														abs(totalEarnings)
												]
											}
											else -> {
												locale[
														"commands.economy.brokerSell.successfullySoldLoss",
														abs(howMuchWillBePaidToTheUser),
														abs(totalEarnings),
														locale["commands.economy.broker.portfolioExample", serverConfig.commandPrefix]
												]
											}
										}
								],
								when {
									totalEarnings == 0L -> Emotes.LORI_SHRUG
									totalEarnings > 0L -> Emotes.LORI_RICH
									else -> Emotes.LORI_CRYING
								}
						)
				)
			}
		}
	}
}