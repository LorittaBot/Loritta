package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.DictionaryExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object DictionaryCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Dictionary

    override fun declaration() = command(listOf("dictionary"), CommandCategory.UTILS, I18N_PREFIX.Description) {
        executor = DictionaryExecutor
    }
}