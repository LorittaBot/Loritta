package net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.GlobalPunishmentRelaySystemPlugin
import net.perfectdreams.loritta.plugin.globalpunishmentrelaysystem.commands.base.DSLCommandBase

object LGPRSCheckGuildCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot, m: GlobalPunishmentRelaySystemPlugin) = create(
			loritta,
			listOf("lgprs check guild")
	) {

	}
}