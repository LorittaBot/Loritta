package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.content
import kotlinx.serialization.json.double
import mu.KotlinLogging
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.SilentCommandException
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.commands.base.DSLCommandBase
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.NumberUtils
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.kotlin.utils.addToStdlib.sumByLong
import kotlin.math.abs

object BrokerSellStockCommand : DSLCommandBase {
	override fun command(plugin: LoriBrokerPlugin, loritta: Loritta) = create(
			loritta,
			plugin.aliases.flatMap { listOf("$it sell", "$it vender") }
	) {
		description { it["commands.economy.brokerSell.description"] }

		arguments {
			argument(ArgumentType.TEXT) {}
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			val tickerId = this.args.getOrNull(0)
					?.toUpperCase()
					?: run { explain(); throw SilentCommandException() }

			if (!plugin.validStocksCodes.any { it == this.args[0] })
				fail(locale["commands.economy.broker.invalidTickerId", locale["commands.economy.brokerBuy.baseExample", serverConfig.commandPrefix]])

			val ticker = plugin.tradingApi
					.getOrRetrieveTicker(tickerId, listOf("lp", "description"))

			if (ticker["current_session"]!!.content != LoriBrokerPlugin.MARKET)
				fail(locale["commands.economy.broker.outOfSession"])

			val quantity = this.args.getOrNull(1) ?: "1"

			val number = NumberUtils.convertShortenedNumberToLong(quantity)
					?: fail(locale["commands.invalidNumber", quantity], Emotes.LORI_CRYING.toString())

			if (0 >= number)
				fail(locale["commands.economy.brokerSell.zeroValue"], Constants.ERROR)

			plugin.mutex.withLock {
				val selfStocks = loritta.newSuspendedTransaction {
					BoughtStocks.select {
						BoughtStocks.user eq user.idLong and (BoughtStocks.ticker eq tickerId)
					}.toList()
				}

				if (number > selfStocks.size)
					fail(locale["commands.economy.brokerSell.notEnoughStocks", tickerId])

				val stocksThatWillBeSold = selfStocks.take(number.toInt())
				val howMuchWillBePaidToTheUser = plugin.convertReaisToSonhos(ticker["lp"]?.double!!) * number

				val totalEarnings = howMuchWillBePaidToTheUser - stocksThatWillBeSold.sumByLong { it[BoughtStocks.price] }

				loritta.newSuspendedTransaction {
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