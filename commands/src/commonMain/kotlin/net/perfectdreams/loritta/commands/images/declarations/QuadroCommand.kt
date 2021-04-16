package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.QuadroExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object QuadroCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.wolverine"

    override fun declaration() = command(listOf("quadro", "frame", "picture", "wolverine")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = QuadroExecutor
    }
}