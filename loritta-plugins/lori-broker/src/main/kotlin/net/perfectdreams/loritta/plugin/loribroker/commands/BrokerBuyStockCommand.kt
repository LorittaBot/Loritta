package net.perfectdreams.loritta.plugin.loribroker.commands

import mu.KotlinLogging
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin

class BrokerBuyStockCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases.flatMap { listOf("$it buy", "$it comprar") }, CommandCategory.ECONOMY) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun command() = create {
		localizedDescription("commands.command.brokerbuy.description")

		arguments {
			argument(ArgumentType.TEXT) {}
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			fail("Agora a corretora está disponível via Slash Commands! Como ela foi refeita para ter menos problemas e mais funcionalidades, tivemos que desativar o sistema antigo da corretora... Por favor, use `/broker buy`!")
		}
	}
}