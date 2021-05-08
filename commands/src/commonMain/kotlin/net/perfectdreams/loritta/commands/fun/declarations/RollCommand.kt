package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.RollExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object RollCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.roll"

    override fun declaration() = command(listOf("roll"), CommandCategory.FUN) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = RollExecutor
    }
}