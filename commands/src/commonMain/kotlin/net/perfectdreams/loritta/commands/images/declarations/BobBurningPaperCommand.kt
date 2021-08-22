package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object BobBurningPaperCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Bobburningpaper

    override fun declaration() = command(listOf("bobburningpaper", "bobpaperfire", "bobfire", "bobpapelfogo", "bobfogo"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = BobBurningPaperExecutor
    }
}