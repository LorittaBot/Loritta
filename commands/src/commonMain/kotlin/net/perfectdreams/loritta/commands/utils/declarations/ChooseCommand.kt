package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.ChooseExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object ChooseCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.choose"

    override fun declaration() = command(listOf("choose", "escolher"), CommandCategory.UTILS) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = ChooseExecutor
    }
}