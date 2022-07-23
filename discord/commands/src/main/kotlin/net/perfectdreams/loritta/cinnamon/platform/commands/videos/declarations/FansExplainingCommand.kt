package net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.FansExplainingExecutor

object FansExplainingCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Fansexplaining

    override fun declaration() = slashCommand(listOf("fansexplaining"), CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = FansExplainingExecutor
    }
}