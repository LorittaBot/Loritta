package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.MemeMakerExecutor

object MemeMakerCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Mememaker

    override fun declaration() = slashCommand(listOf("mememaker"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = MemeMakerExecutor
    }
}