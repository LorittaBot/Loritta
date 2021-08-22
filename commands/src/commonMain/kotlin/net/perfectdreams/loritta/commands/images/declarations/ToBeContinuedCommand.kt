package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.ToBeContinuedExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object ToBeContinuedCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Tobecontinued

    override fun declaration() = command(listOf("tobecontinued"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = ToBeContinuedExecutor
    }
}