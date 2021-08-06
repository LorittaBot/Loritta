package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object AnagramCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.anagram"

    override fun declaration() = command(listOf("anagram", "anagrama"), CommandCategory.UTILS, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = AnagramExecutor
    }
}