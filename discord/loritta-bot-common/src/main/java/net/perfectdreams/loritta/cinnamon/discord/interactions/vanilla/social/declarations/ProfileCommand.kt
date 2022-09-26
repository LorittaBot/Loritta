package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.profile.AboutMeExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.profile.ProfileExecutor
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData

class ProfileCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Profile
        val PROFILE_VIEW_I18N_PREFIX = I18nKeysData.Commands.Command.Profileview
        val ABOUT_ME_I18N_PREFIX = I18nKeysData.Commands.Command.Aboutme
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        subcommand(PROFILE_VIEW_I18N_PREFIX.Label, PROFILE_VIEW_I18N_PREFIX.Description) {
            executor = { ProfileExecutor(it) }
        }

        subcommand(ABOUT_ME_I18N_PREFIX.Label, ABOUT_ME_I18N_PREFIX.Description) {
            executor = { AboutMeExecutor(it) }
        }
    }
}