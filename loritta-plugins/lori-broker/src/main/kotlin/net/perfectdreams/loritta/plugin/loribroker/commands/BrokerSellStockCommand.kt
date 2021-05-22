package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.utils.Constants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.GenericReplies
import net.perfectdreams.loritta.utils.NumberUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason
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
		localizedDescription("commands.command.brokersell.description")

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
				fail(locale["commands.command.broker.invalidTickerId", locale["commands.command.brokerbuy.baseExample", serverConfig.commandPrefix]])

			val ticker = plugin.tradingApi
					.getOrRetrieveTicker(tickerId, listOf(LoriBrokerPlugin.CURRENT_PRICE_FIELD, "description"))

			if (ticker["current_session"]!!.jsonPrimitive.content != LoriBrokerPlugin.MARKET)
				fail(locale["commands.command.broker.outOfSession"])

			val mutex = plugin.mutexes.getOrPut(user.idLong, { Mutex() })
			if (mutex.isLocked)
				fail(locale["commands.command.broker.alreadyExecutingAction"])

			var quantity = this.args.getOrNull(1) ?: "1"

			if (quantity == "all") {
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
				fail(locale["commands.command.brokersell.zeroValue"], Constants.ERROR)

			mutex.withLock {
				val selfStocks = loritta.newSuspendedTransaction {
					BoughtStocks.select {
						BoughtStocks.user eq user.idLong and (BoughtStocks.ticker eq tickerId)
					}.toList()
				}

				if (number > selfStocks.size)
					fail(locale["commands.command.brokersell.notEnoughStocks", tickerId])

				val stocksThatWillBeSold = selfStocks.take(number.toInt())
				val howMuchWillBePaidToTheUser = plugin.convertToSellingPrice(
						plugin.convertReaisToSonhos(ticker[LoriBrokerPlugin.CURRENT_PRICE_FIELD]?.jsonPrimitive?.double!!)
				) * number

				logger.info { "User ${this.user.idLong} is trying to sell $number $tickerId for $howMuchWillBePaidToTheUser" }
				
				val totalEarnings = howMuchWillBePaidToTheUser - stocksThatWillBeSold.sumByLong { it[BoughtStocks.price] }

				// The reason we batch the stocks in multiple transactions is due to this issue:
				// https://github.com/LorittaBot/Loritta/issues/2343
				// https://stackoverflow.com/questions/49274390/postgresql-and-hibernate-java-io-ioexception-tried-to-send-an-out-of-range-inte
				stocksThatWillBeSold.chunked(32767).forEachIndexed { index, chunkedStocks ->
					loritta.newSuspendedTransaction {
						// To avoid selling "phantom" stocks, we need to check if the user has enough stocks inside the transaction
						// If the stock value changed, then it means that the user bought (or sold!) stocks while this transaction was being executed
						//
						// When checking for the bought stocks, we need to subtract the "already removed" chunked stocks
						val canBeExecuted = BoughtStocks.select {
							BoughtStocks.user eq user.idLong and (BoughtStocks.ticker eq tickerId)
						}.count() == (selfStocks.size.toLong() - (index * 32767))

						if (!canBeExecuted)
							fail(locale["commands.command.brokersell.boughtStocksWhileSelling"])

						BoughtStocks.deleteWhere {
							BoughtStocks.id inList chunkedStocks.map { it[BoughtStocks.id] }
						}
					}
				}

				// We add the sonhos in a separate transaction because we don't wanna to add them within that loop above
				loritta.newSuspendedTransaction {
					lorittaUser.profile.addSonhosAndAddToTransactionLogNested(
						howMuchWillBePaidToTheUser,
						SonhosPaymentReason.STOCKS
					)
				}

				logger.info { "User ${this.user.idLong} sold $number $tickerId for $howMuchWillBePaidToTheUser" }

				reply(
						LorittaReply(
								locale[
										"commands.command.brokersell.successfullySold",
										stocksThatWillBeSold.size,
										locale["commands.command.broker.stocks.${if (stocksThatWillBeSold.size == 1) "one" else "multiple"}"],
										tickerId,
										when {
											totalEarnings == 0L -> {
												locale[
														"commands.command.brokersell.successfullySoldNeutral"
												]
											}
											totalEarnings > 0L -> {
												locale[
														"commands.command.brokersell.successfullySoldProfit",
														abs(howMuchWillBePaidToTheUser),
														abs(totalEarnings)
												]
											}
											else -> {
												locale[
														"commands.command.brokersell.successfullySoldLoss",
														abs(howMuchWillBePaidToTheUser),
														abs(totalEarnings),
														locale["commands.command.broker.portfolioExample", serverConfig.commandPrefix]
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