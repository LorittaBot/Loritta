package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.InvertColorsExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object InvertColorsCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.invert"

    override fun declaration() = command(listOf("invert", "inverter"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = InvertColorsExecutor
    }
}