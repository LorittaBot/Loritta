package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import mu.KotlinLogging
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class BrokerBuyStockCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(
    loritta,
    BrokerCommand.ALIASES.flatMap { listOf("$it buy", "$it comprar") },
    net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY
) {
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