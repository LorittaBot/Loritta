package net.perfectdreams.loritta.plugin.loribroker.commands

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin

class BrokerPortfolioCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases.flatMap { listOf("$it portfolio", "$it portfólio", "$it p") }, CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.command.brokerportfolio.description")

		executesDiscord {
			fail("Agora a corretora está disponível via Slash Commands! Como ela foi refeita para ter menos problemas e mais funcionalidades, tivemos que desativar o sistema antigo da corretora... Por favor, use `/broker portfolio`!")
		}
	}
}