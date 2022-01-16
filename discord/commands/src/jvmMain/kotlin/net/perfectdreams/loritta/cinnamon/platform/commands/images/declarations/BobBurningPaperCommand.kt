package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.BobBurningPaperExecutor

object BobBurningPaperCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Bobburningpaper

    override fun declaration() = slashCommand(listOf("bobburningpaper", "bobpaperfire", "bobfire", "bobpapelfogo", "bobfogo"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = BobBurningPaperExecutor
    }
}