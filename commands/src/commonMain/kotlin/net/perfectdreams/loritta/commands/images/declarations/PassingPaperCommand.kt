package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.PassingPaperExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object PassingPaperCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.passingpaper"

    override fun declaration() = command(listOf("passingpaper", "bilhete", "quizkid"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = PassingPaperExecutor
    }
}