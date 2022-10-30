package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.JankenponExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData

class JankenponCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Jankenpon
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = { JankenponExecutor(it) }
    }
}