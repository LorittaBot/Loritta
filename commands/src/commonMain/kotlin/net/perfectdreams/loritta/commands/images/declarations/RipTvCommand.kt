package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.RipTvExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object RipTvCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.riptv"

    override fun declaration() = command(listOf("riptv")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = RipTvExecutor
    }
}