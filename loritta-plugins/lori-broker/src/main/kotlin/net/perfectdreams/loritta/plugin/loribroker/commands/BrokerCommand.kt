package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.LorittaLauncher
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.selectAll

class BrokerCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases, CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.command.broker.description")

		executesDiscord {
			if (this.args.getOrNull(0) == "sell_all" && this.user.idLong == 123170274651668480L) {
				val byUser = mutableMapOf<Long, Long>()

				loritta.newSuspendedTransaction {
					val x = BoughtStocks.selectAll()

					x.forEach {
						byUser[it[BoughtStocks.user]] = byUser.getOrPut(it[BoughtStocks.user], { 0 }) + it[BoughtStocks.price]
					}
				}

				for ((user, track) in byUser) {
					loritta.newSuspendedTransaction {
						val profile = LorittaLauncher.loritta._getLorittaProfile(user)

						profile?.addSonhosAndAddToTransactionLogNested(
							track,
							SonhosPaymentReason.STOCKS
						)
					}
				}

				sendMessage("Todas as ações foram vendidas!")
				return@executesDiscord
			}

			val stocks = plugin.validStocksCodes.map {
				plugin.tradingApi.getOrRetrieveTicker(
						it,
						listOf(
								LoriBrokerPlugin.CURRENT_PRICE_FIELD,
								"description",
								"current_session"
						)
				)
			}

			val embed = plugin.getBaseEmbed()
					.setTitle("${Emotes.LORI_STONKS} ${locale["commands.command.broker.title"]}")
					.setDescription(
							locale.getList(
									"commands.command.broker.explanation",
									locale["commands.command.broker.buyExample", serverConfig.commandPrefix],
									locale["commands.command.broker.sellExample", serverConfig.commandPrefix],
									locale["commands.command.broker.portfolioExample", serverConfig.commandPrefix],
									Emotes.DO_NOT_DISTURB,
									Emotes.LORI_CRYING
							).joinToString("\n")
					)
					.setFooter(locale["commands.command.broker.footer"])

			// Sorted by the ticker name
			for (stock in stocks.sortedBy { it["short_name"]!!.jsonPrimitive.content }) {
				val tickerId = stock["short_name"]!!.jsonPrimitive.content
				val tickerName = plugin.trackedTickerCodes[tickerId]
				val currentPrice = plugin.convertReaisToSonhos(stock[LoriBrokerPlugin.CURRENT_PRICE_FIELD]?.jsonPrimitive?.double!!)

				val buyingPrice = plugin.convertToBuyingPrice(currentPrice)
				val sellingPrice = plugin.convertToSellingPrice(currentPrice)
				val changePercentage = stock["chp"]?.jsonPrimitive?.double!!

				if (stock["current_session"]!!.jsonPrimitive.content != LoriBrokerPlugin.MARKET)
					embed.addField(
							"${Emotes.DO_NOT_DISTURB} `${stock["short_name"]?.jsonPrimitive?.content}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
							locale["commands.command.broker.priceBeforeMarketClose", plugin.convertReaisToSonhos(stock[LoriBrokerPlugin.CURRENT_PRICE_FIELD]?.jsonPrimitive?.double!!)],
							true
					)
				else
					embed.addField(
							"${Emotes.ONLINE} `${stock["short_name"]?.jsonPrimitive?.content}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
							"""${locale["commands.command.broker.buyPrice", buyingPrice]}
							  |${locale["commands.command.broker.sellPrice", sellingPrice]}
							""".trimMargin(),
							true
					)
			}

			sendMessage(embed.build())
		}
	}
}