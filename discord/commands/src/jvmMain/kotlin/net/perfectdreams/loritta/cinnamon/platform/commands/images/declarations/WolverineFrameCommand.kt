package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.WolverineFrameExecutor

object WolverineFrameCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Wolverineframe

    override fun declaration() = slashCommand(listOf("wolverineframe"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = WolverineFrameExecutor
    }
}