package net.perfectdreams.loritta.plugin.helpinghands

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.helpinghands.commands.CoinFlipBetCommand
import net.perfectdreams.loritta.plugin.helpinghands.commands.CoinFlipBetStatsCommand
import net.perfectdreams.loritta.plugin.helpinghands.commands.DailyInactivityTaxExecutor
import net.perfectdreams.loritta.plugin.helpinghands.commands.EmojiFightBetCommand
import net.perfectdreams.loritta.plugin.helpinghands.commands.EmojiFightCommand
import net.perfectdreams.loritta.plugin.helpinghands.commands.GuessNumberCommand
import net.perfectdreams.loritta.plugin.helpinghands.commands.RepListCommand
import net.perfectdreams.loritta.plugin.helpinghands.utils.DailyInactivityTaxUtils

class HelpingHandsPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
	override fun onEnable() {
		registerCommands(
				CoinFlipBetStatsCommand(this),
				CoinFlipBetCommand(this),
				RepListCommand(this),
				GuessNumberCommand(this),
				EmojiFightCommand(this),
				EmojiFightBetCommand(this)
		)

		if (loritta.isMaster)
			launch(DailyInactivityTaxUtils.createAutoInactivityTask())
		this.loriToolsExecutors += DailyInactivityTaxExecutor
	}

	override fun onDisable() {
		super.onDisable()
	}
}