package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class BrokerCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, ALIASES, net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	companion object {
		val ALIASES = listOf(
			"broker",
			"corretora"
		)
	}

	override fun command() = create {
		localizedDescription("commands.command.broker.description")

		executesDiscord {
			fail("Agora a corretora está disponível via Slash Commands! Como ela foi refeita para ter menos problemas e mais funcionalidades, tivemos que desativar o sistema antigo da corretora... Por favor, use `/broker info`!")
		}
	}
}