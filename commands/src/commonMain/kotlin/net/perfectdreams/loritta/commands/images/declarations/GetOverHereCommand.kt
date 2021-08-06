package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.GetOverHereExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object GetOverHereCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.getoverhere"

    override fun declaration() = command(listOf("getoverhere"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = GetOverHereExecutor
    }
}