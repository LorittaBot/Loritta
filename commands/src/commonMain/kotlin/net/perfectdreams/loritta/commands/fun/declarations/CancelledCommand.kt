package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.CancelledExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object CancelledCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Cancelled

    override fun declaration() = command(listOf("cancelled", "cancelado", "cancel", "cancelar"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = CancelledExecutor
    }
}