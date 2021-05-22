package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object AnagramCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.anagram"

    override fun declaration() = command(listOf("anagram", "anagrama"), CommandCategory.UTILS) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = AnagramExecutor
    }
}