package net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.CoinflipBetGlobalExecutor

object BetCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Broker

    override fun declaration() = slashCommand(listOf("bet"), CommandCategory.ECONOMY, TodoFixThisData) {
        subcommandGroup(listOf("coinflip"), TodoFixThisData) {
            subcommand(listOf("global"), TodoFixThisData) {
                executor = CoinflipBetGlobalExecutor
            }
        }
    }
}