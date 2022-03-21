package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.platform.commands.images.MarkMetaExecutor

object MarkMetaCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Markmeta

    override fun declaration() = slashCommand(listOf("markmeta"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = MarkMetaExecutor
    }
}