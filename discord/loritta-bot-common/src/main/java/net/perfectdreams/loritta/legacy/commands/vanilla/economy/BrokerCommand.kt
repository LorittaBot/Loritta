package net.perfectdreams.loritta.legacy.commands.vanilla.economy

import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase

class BrokerCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, ALIASES, CommandCategory.ECONOMY) {
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