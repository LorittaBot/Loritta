package net.perfectdreams.loritta.commands.utils.declarations

import net.perfectdreams.loritta.commands.utils.MorseExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object MorseCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.morse"

    override fun declaration(): CommandDeclarationBuilder = command(listOf("morse")) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = MorseExecutor
    }

}