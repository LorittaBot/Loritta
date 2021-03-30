package net.perfectdreams.loritta.plugin.apionlytest

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.utils.Emotes

class APIOnlyPlugin(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
	override fun onEnable() {
		registerCommand(command(loritta, "APIOnlyTestCommand", listOf("apionlytest"), CommandCategory.MAGIC) {
			executes {
				reply(
						LorittaReply(
								"Teste de um comando compilado *apenas* com a API da Loritta!",
								Emotes.DEFAULT_DANCE.toString()
						)
				)
			}
		})
	}
}