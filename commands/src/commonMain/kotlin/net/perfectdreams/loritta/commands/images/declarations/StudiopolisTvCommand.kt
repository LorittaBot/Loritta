package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.StudiopolisTvExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object StudiopolisTvCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.studiopolistv"

    override fun declaration() = command(listOf("studiopolistv", "studiopolis")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = StudiopolisTvExecutor
    }
}