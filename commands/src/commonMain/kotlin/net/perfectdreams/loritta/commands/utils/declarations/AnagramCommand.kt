package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object AnagramCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Anagram

    override fun declaration() = command(listOf("anagram", "anagrama"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = AnagramExecutor
    }
}