package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.InvertColorsExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object InvertColorsCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Invertcolors

    override fun declaration() = command(listOf("invert", "inverter"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = InvertColorsExecutor
    }
}