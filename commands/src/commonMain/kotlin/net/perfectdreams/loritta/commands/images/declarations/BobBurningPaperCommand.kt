package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BobBurningPaperExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object BobBurningPaperCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.bobfire"

    override fun declaration() = command(listOf("bobburningpaper", "bobpaperfire", "bobfire", "bobpapelfogo", "bobfogo"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = BobBurningPaperExecutor
    }
}