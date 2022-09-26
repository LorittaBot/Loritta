package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import mu.KotlinLogging
import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.api.commands.arguments
import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class BrokerSellStockCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, BrokerCommand.ALIASES.flatMap { listOf("$it sell", "$it vender") }, net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun command() = create {
		localizedDescription("commands.command.brokersell.description")

		arguments {
			argument(ArgumentType.TEXT) {}
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			fail("Agora a corretora está disponível via Slash Commands! Como ela foi refeita para ter menos problemas e mais funcionalidades, tivemos que desativar o sistema antigo da corretora... Por favor, use `/broker sell`!")
		}
	}
}