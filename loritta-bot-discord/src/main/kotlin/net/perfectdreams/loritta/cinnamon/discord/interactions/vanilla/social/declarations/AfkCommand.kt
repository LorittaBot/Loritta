package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.AfkOffExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.AfkOnExecutor

class AfkCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Afk
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.SOCIAL, TodoFixThisData) {
        subcommand(I18N_PREFIX.On.Label, I18N_PREFIX.On.Description) {
            executor = { AfkOnExecutor(it) }
        }

        subcommand(I18N_PREFIX.Off.Label, I18N_PREFIX.Off.Description) {
            executor = { AfkOffExecutor(it) }
        }
    }
}