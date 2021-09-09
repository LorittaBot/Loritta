package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.images.ToBeContinuedExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object ToBeContinuedCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Tobecontinued

    override fun declaration() = command(listOf("tobecontinued"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = ToBeContinuedExecutor
    }
}