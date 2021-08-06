package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.FaustaoExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object FaustaoCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.faustao"

    override fun declaration() = command(listOf("faustão", "faustao"), CommandCategory.FUN, LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
        executor = FaustaoExecutor
    }
}