package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.InvertColorsExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object InvertColorsCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.invert"

    override fun declaration() = command(listOf("invert", "inverter"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = InvertColorsExecutor
    }
}