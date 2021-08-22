package net.perfectdreams.loritta.commands.economy.declarations

import net.perfectdreams.loritta.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object SonhosCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Sonhos

    override fun declaration() = command(listOf("sonhos", "atm"), CommandCategory.ECONOMY, I18N_PREFIX.Description) {
        executor = SonhosExecutor
    }
}