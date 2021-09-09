package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.images.RipTvExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object RipTvCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Riptv

    override fun declaration() = command(listOf("riptv"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = RipTvExecutor
    }
}