package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.HelpExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object HelpCommand: CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.help"

    override fun declaration(): CommandDeclarationBuilder = command(listOf("help", "commands", "comandos"), CommandCategory.UTILS, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = HelpExecutor
    }
}