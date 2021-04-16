package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object PassingPaperCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.passingpaper"

    override fun declaration() = command(listOf("passingpaper", "bilhete", "quizkid")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = PassingPaperExecutor
    }
}