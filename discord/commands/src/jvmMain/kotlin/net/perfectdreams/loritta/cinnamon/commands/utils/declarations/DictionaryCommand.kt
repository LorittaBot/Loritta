package net.perfectdreams.loritta.cinnamon.commands.utils.declarations

import net.perfectdreams.loritta.cinnamon.commands.utils.DictionaryExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object DictionaryCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Dictionary

    override fun declaration() = command(listOf("dictionary"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = DictionaryExecutor
    }
}