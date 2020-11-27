package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.LorittaLauncher
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class BrokerCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases, CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.economy.broker.description")

		executesDiscord {
			if (this.args.getOrNull(0) == "sell_all" && this.user.idLong == 123170274651668480L) {
				val byUser = mutableMapOf<Long, Long>()

				newSuspendedTransaction {
					val x = BoughtStocks.selectAll()

					x.forEach {
						byUser[it[BoughtStocks.user]] = byUser.getOrPut(it[BoughtStocks.user], { 0 }) + it[BoughtStocks.price]
					}
				}

				for ((user, track) in byUser) {
					newSuspendedTransaction {
						val profile = LorittaLauncher.loritta._getLorittaProfile(user)

						if (profile != null) {
							profile.addSonhosNested(
									track
							)

							PaymentUtils.addToTransactionLogNested(
									track,
									SonhosPaymentReason.STOCKS,
									receivedBy = user
							)
						}
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
								LoriBrokerPlugin.BUYING_PRICE_FIELD,
								LoriBrokerPlugin.SELLING_PRICE_FIELD,
								"description",
								"current_session"
						)
				)
			}

			val embed = plugin.getBaseEmbed()
					.setTitle(locale["commands.economy.broker.title"])
					.setDescription(
							locale.getList(
									"commands.economy.broker.explanation",
									locale["commands.economy.broker.buyExample", serverConfig.commandPrefix],
									locale["commands.economy.broker.sellExample", serverConfig.commandPrefix],
									locale["commands.economy.broker.portfolioExample", serverConfig.commandPrefix],
									Emotes.DO_NOT_DISTURB,
									Emotes.LORI_CRYING
							).joinToString("\n")
					)

			// Sorted by the ticker name
			for (stock in stocks.sortedBy { it["short_name"]!!.jsonPrimitive.content }) {
				val tickerId = stock["short_name"]!!.jsonPrimitive.content
				val tickerName = plugin.trackedTickerCodes[tickerId]

				if (stock["current_session"]!!.jsonPrimitive.content != LoriBrokerPlugin.MARKET)
					embed.addField(
							"${Emotes.DO_NOT_DISTURB} `${stock["short_name"]?.jsonPrimitive?.content}` ($tickerName)",
							"**Preço antes do fechamento:** ${plugin.convertReaisToSonhos(stock[LoriBrokerPlugin.CURRENT_PRICE_FIELD]?.jsonPrimitive?.double!!)} sonhos",
							true
					)
				else
					embed.addField(
							"${Emotes.ONLINE} `${stock["short_name"]?.jsonPrimitive?.content}` ($tickerName)",
							"""**Compra:**  ${plugin.convertReaisToSonhos(stock[LoriBrokerPlugin.BUYING_PRICE_FIELD]?.jsonPrimitive?.double!!)}
							  |**Venda:**  ${plugin.convertReaisToSonhos(stock[LoriBrokerPlugin.SELLING_PRICE_FIELD]?.jsonPrimitive?.double!!)}
							""".trimMargin(),
							true
					)
			}

			sendMessage(embed.build())
		}
	}
}