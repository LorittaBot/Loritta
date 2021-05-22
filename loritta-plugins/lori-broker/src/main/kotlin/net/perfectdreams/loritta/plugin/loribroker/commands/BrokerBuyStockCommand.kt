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
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select

class BrokerBuyStockCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases.flatMap { listOf("$it buy", "$it comprar") }, CommandCategory.ECONOMY) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun command() = create {
		localizedDescription("commands.command.brokerbuy.description")

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
		}
	}
}