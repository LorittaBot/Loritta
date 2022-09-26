package net.perfectdreams.loritta.legacy.commands.vanilla.economy

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase

class BrokerPortfolioCommand(val plugin: LorittaDiscord) : DiscordAbstractCommandBase(plugin, BrokerCommand.ALIASES.flatMap { listOf("$it portfolio", "$it portfólio", "$it p") }, net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.command.brokerportfolio.description")

		executesDiscord {
			fail("Agora a corretora está disponível via Slash Commands! Como ela foi refeita para ter menos problemas e mais funcionalidades, tivemos que desativar o sistema antigo da corretora... Por favor, use `/broker portfolio`!")
		}
	}
}