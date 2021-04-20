package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object JankenponCommand: CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.rockpaperscissors"
    override fun declaration(): CommandDeclarationBuilder = command(listOf("jankenpon", "pedrapapeltesoura", "ppt"), CommandCategory.FUN) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = JankenponExecutor
    }
}