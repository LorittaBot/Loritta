package net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.TransactionsExecutor

object TransactionsCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Transactions

    override fun declaration() = command(listOf("transactions"), CommandCategory.ECONOMY, I18N_PREFIX.Description) {
        executor = TransactionsExecutor
    }
}