package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.BrokerInfo.brokerBaseEmbed

class BrokerInfoExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(BrokerInfoExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val stockInformations = context.loritta.services.bovespaBroker.getAllTickers()

        context.sendMessage {
            brokerBaseEmbed {
                // TODO: Localization
                title = "${Emotes.LoriStonks} commands.command.broker.title"
                description = "commands.command.broker.explanation"
                footer("commands.command.broker.footer")

                for (stockInformation in stockInformations.sortedByDescending { it.ticker }) {
                    val tickerId = stockInformation.ticker
                    val tickerName = BrokerInfo.trackedTickerCodes[tickerId]
                    val currentPrice = BrokerInfo.convertReaisToSonhos(stockInformation.value)
                    val buyingPrice = BrokerInfo.convertToBuyingPrice(currentPrice) // Buying price
                    val sellingPrice = BrokerInfo.convertToSellingPrice(currentPrice) // Selling price

                    val changePercentage = stockInformation.dailyPriceVariation

                    // TODO: Emotes on the field title
                    val fieldTitle = "`$tickerId` ($tickerName) | ${"%.2f".format(changePercentage)}%"

                    // TODO: Constant and other statuses
                    if (stockInformation.status != "market") {
                        field {
                            name = fieldTitle
                            value = "**Preço antes do fechamento:** $currentPrice"
                            inline = true
                        }
                    } else {
                        field {
                            name = fieldTitle
                            value = """**Compra:** $buyingPrice
                                |**Venda:** $sellingPrice
                            """.trimMargin()
                            inline = true
                        }
                    }
                    /* if (stockInformation["current_session"]!!.jsonPrimitive.content != LoriBrokerPlugin.MARKET)
                        embed.addField(
                            "${Emotes.DO_NOT_DISTURB} `${stockInformation["short_name"]?.jsonPrimitive?.content}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                            locale["commands.command.broker.priceBeforeMarketClose", plugin.convertReaisToSonhos(stockInformation[LoriBrokerPlugin.CURRENT_PRICE_FIELD]?.jsonPrimitive?.double!!)],
                            true
                        )
                    else
                        embed.addField(
                            "${Emotes.ONLINE} `${stockInformation["short_name"]?.jsonPrimitive?.content}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                            """${locale["commands.command.broker.buyPrice", buyingPrice]}
							  |${locale["commands.command.broker.sellPrice", sellingPrice]}
							""".trimMargin(),
                            true
                        ) */
                }
            }
        }
        /* val stocks = plugin.validStocksCodes.map {
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
			*/
    }
}