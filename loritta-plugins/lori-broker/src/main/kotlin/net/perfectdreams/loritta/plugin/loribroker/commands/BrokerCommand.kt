package net.perfectdreams.loritta.plugin.loribroker.commands

import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.utils.Emotes

class BrokerCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases, CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.economy.broker.description")

		executesDiscord {
			val stocks = plugin.validStocksCodes.map {
				plugin.tradingApi.getOrRetrieveTicker(
						it,
						listOf("lp", "description", "current_session")
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

			for (stock in stocks) {
				val tickerId = stock["short_name"]!!.jsonPrimitive.content
				val tickerName = plugin.fancyTickerNames[tickerId]

				if (stock["current_session"]!!.jsonPrimitive.content != LoriBrokerPlugin.MARKET)
					embed.addField(
							"${Emotes.DO_NOT_DISTURB} `${stock["short_name"]?.jsonPrimitive?.content}` ($tickerName)",
							"${plugin.convertReaisToSonhos(stock["lp"]?.jsonPrimitive?.double!!)} sonhos",
							true
					)
				else
					embed.addField(
							"${Emotes.ONLINE} `${stock["short_name"]?.jsonPrimitive?.content}` ($tickerName)",
							"${plugin.convertReaisToSonhos(stock["lp"]?.jsonPrimitive?.double!!)} sonhos",
							true
					)
			}

			sendMessage(embed.build())
		}
	}
}