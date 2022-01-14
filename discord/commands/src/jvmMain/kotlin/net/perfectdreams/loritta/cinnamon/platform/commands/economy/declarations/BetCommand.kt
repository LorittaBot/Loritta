package net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.CoinflipBetGlobalExecutor

object BetCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Broker

    override fun declaration() = command(listOf("bet"), CommandCategory.ECONOMY, TodoFixThisData) {
        subcommandGroup(listOf("coinflip"), TodoFixThisData) {
            subcommand(listOf("global"), TodoFixThisData) {
                executor = CoinflipBetGlobalExecutor
            }
        }
    }
}