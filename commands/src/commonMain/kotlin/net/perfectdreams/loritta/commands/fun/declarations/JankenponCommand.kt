package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object JankenponCommand: CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.rockpaperscissors"
    override fun declaration(): CommandDeclarationBuilder = command(listOf("jankenpon", "pedrapapeltesoura", "ppt"), CommandCategory.FUN, LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
        executor = JankenponExecutor
    }
}