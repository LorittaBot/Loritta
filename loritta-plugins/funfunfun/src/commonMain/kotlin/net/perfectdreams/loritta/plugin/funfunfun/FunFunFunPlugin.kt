package net.perfectdreams.loritta.plugin.funfunfun

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.plugin.funfunfun.commands.CancelledCommand

class FunFunFunPlugin(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
	override fun onEnable() {
		registerCommands(
				CancelledCommand(this)
		)
		platformSpecificOnEnable()
	}
}