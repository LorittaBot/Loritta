package net.perfectdreams.loritta.cinnamon.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.commands.utils.AnagramExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object AnagramCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Anagram

    override fun declaration() = command(listOf("anagram", "anagrama"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = AnagramExecutor
    }
}