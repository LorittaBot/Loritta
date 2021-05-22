package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.MorseFromExecutor
import net.perfectdreams.loritta.commands.utils.MorseToExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object MorseCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.morse"

    override fun declaration(): CommandDeclarationBuilder = command(listOf("morse"), CommandCategory.UTILS) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommand(listOf("to")) {
            description = LocaleKeyData("${LOCALE_PREFIX}.description")
            executor = MorseToExecutor
        }
        subcommand(listOf("from")) {
            description = LocaleKeyData("${LOCALE_PREFIX}.description")
            executor = MorseFromExecutor
        }
    }
}