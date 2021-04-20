package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object RomeroBrittoCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.romerobritto"

    override fun declaration() = command(listOf("romerobritto", "pintura", "painting"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = RomeroBrittoExecutor
    }
}