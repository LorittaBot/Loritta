package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.TrumpExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object TrumpCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.trump"

    override fun declaration() = command(listOf("trump"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = TrumpExecutor
    }
}