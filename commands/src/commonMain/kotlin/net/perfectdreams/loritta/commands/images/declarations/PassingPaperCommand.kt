package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object PassingPaperCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Passingpaper

    override fun declaration() = command(listOf("passingpaper", "bilhete", "quizkid"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = PassingPaperExecutor
    }
}