package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.WolverineFrameExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object WolverineFrameCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Wolverineframe

    override fun declaration() = command(listOf("wolverineframe"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = WolverineFrameExecutor
    }
}