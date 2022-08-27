package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.AboutMeExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.ProfileExecutor
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData

class ProfileCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Profile
        val ABOUT_ME_I18N_PREFIX = I18nKeysData.Commands.Command.Aboutme
    }

    override fun declaration() = slashCommand("profile", CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        subcommand("view", TodoFixThisData) {
            executor = { ProfileExecutor(it) }
        }

        subcommand("aboutme", ABOUT_ME_I18N_PREFIX.Description) {
            executor = { AboutMeExecutor(it) }
        }
    }
}