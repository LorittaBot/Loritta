package net.perfectdreams.loritta.plugin.helpinghands

import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.helpinghands.commands.CoinFlipBetCommand
import net.perfectdreams.loritta.plugin.helpinghands.commands.DailyInactivityTaxExecutor
import net.perfectdreams.loritta.plugin.helpinghands.commands.RepListCommand
import net.perfectdreams.loritta.plugin.helpinghands.utils.DailyInactivityTaxUtils

class HelpingHandsPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	override fun onEnable() {
		loritta as LorittaDiscord

		registerCommand(CoinFlipBetCommand.command(this, loritta))
		registerCommand(RepListCommand.create(loritta))

		if (loritta.isMaster)
			launch(DailyInactivityTaxUtils.createAutoInactivityTask())
		this.loriToolsExecutors += DailyInactivityTaxExecutor
	}


	override fun onDisable() {
		super.onDisable()
	}

	companion object {
		private val logger = KotlinLogging.logger {}
	}
}