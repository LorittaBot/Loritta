package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.DictionaryExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object DictionaryCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.dicio"

    override fun declaration() = command(listOf("dictionary"), CommandCategory.UTILS) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = DictionaryExecutor
    }
}