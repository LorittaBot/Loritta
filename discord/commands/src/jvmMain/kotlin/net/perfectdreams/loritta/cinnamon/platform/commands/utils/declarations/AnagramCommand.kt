package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.AnagramExecutor

object AnagramCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Anagram

    override fun declaration() = slashCommand(listOf("anagram", "anagrama"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = AnagramExecutor
    }
}