package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.DictionaryExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object DictionaryCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.dicio"

    override fun declaration() = command(listOf("dictionary"), CommandCategory.UTILS, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = DictionaryExecutor
    }
}