package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.serialization.json.content
import kotlinx.serialization.json.double
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.commands.base.DSLCommandBase
import net.perfectdreams.loritta.utils.Emotes

object BrokerCommand : DSLCommandBase {
	override fun command(plugin: LoriBrokerPlugin, loritta: Loritta) = create(
			loritta,
			plugin.aliases
	) {
		description { it["commands.economy.broker.description"] }

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
				val tickerId = stock["short_name"]!!.content
				val tickerName = plugin.fancyTickerNames[tickerId]

				if (stock["current_session"]!!.content != LoriBrokerPlugin.MARKET)
					embed.addField(
							"${Emotes.DO_NOT_DISTURB} `${stock["short_name"]?.content}` ($tickerName)",
							"${plugin.convertReaisToSonhos(stock["lp"]?.double!!)} sonhos",
							true
					)
				else
					embed.addField(
							"${Emotes.ONLINE} `${stock["short_name"]?.content}` ($tickerName)",
							"${plugin.convertReaisToSonhos(stock["lp"]?.double!!)} sonhos",
							true
					)
			}

			sendMessage(embed.build())
		}
	}
}