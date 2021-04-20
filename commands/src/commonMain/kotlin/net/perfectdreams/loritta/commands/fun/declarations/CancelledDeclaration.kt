package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.CancelledExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object CancelledDeclaration : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.cancelled"

    override fun declaration() = command(listOf("cancelled", "cancelado", "cancel", "cancelar"), CommandCategory.FUN) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = CancelledExecutor
    }
}