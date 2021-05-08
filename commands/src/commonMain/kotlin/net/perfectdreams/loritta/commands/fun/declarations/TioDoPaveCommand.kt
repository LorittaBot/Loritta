package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.TioDoPaveExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object TioDoPaveCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.tiodopave"

    override fun declaration() = command(listOf("tiodopave"), CommandCategory.FUN) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = TioDoPaveExecutor
    }
}