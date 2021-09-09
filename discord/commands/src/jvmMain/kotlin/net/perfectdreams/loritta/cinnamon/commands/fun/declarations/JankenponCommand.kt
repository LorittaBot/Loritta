package net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.commands.`fun`.JankenponExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object JankenponCommand: CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Jankenpon

    override fun declaration(): CommandDeclarationBuilder = command(listOf("jankenpon", "pedrapapeltesoura", "ppt"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = JankenponExecutor
    }
}