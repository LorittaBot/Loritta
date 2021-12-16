package net.perfectdreams.loritta.plugin.loribroker

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerBuyStockCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerPortfolioCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerSellStockCommand

class LoriBrokerPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
	val aliases = listOf(
			"broker",
			"corretora"
	)

	override fun onEnable() {
		registerCommand(BrokerCommand(this))
		registerCommand(BrokerBuyStockCommand(this))
		registerCommand(BrokerSellStockCommand(this))
		registerCommand(BrokerPortfolioCommand(this))
	}
}