package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object MorseCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.morse"

    override fun declaration(): CommandDeclarationBuilder = command(listOf("morse"), CommandCategory.UTILS, LocaleKeyData("TODO_FIX_THIS").toI18nHelper()) {

        subcommand(listOf("to"), LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
            executor = MorseToExecutor
        }
        subcommand(listOf("from"), LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
            executor = MorseFromExecutor
        }
    }
}