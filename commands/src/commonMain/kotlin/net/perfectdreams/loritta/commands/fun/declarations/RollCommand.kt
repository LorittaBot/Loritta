package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.RollExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object RollCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.roll"

    override fun declaration() = command(listOf("roll"), CommandCategory.FUN, LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
        executor = RollExecutor
    }
}