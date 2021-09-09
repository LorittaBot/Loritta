package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object PassingPaperCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Passingpaper

    override fun declaration() = command(listOf("passingpaper", "bilhete", "quizkid"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = PassingPaperExecutor
    }
}