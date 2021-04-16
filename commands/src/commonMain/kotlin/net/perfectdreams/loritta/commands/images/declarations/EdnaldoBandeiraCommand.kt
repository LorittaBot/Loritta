package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object EdnaldoBandeiraCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.ednaldobandeira"

    override fun declaration() = command(listOf("ednaldobandeira")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = EdnaldoBandeiraExecutor
    }
}