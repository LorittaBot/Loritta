package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object BuckShirtCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.buckshirt"

    override fun declaration() = command(listOf("buckshirt", "buckcamisa"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = BuckShirtExecutor
    }
}