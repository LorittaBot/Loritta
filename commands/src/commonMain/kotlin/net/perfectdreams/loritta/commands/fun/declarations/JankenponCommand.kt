package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.i18n.I18nKeysData

object JankenponCommand: CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Jankenpon

    override fun declaration(): CommandDeclarationBuilder = command(listOf("jankenpon", "pedrapapeltesoura", "ppt"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = JankenponExecutor
    }
}