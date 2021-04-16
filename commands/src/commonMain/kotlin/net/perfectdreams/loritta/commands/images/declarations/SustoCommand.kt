package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.SustoExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object SustoCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.susto"

    override fun declaration() = command(listOf("scared", "fright", "susto")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = SustoExecutor
    }
}