package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.AfkOffExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.AfkOnExecutor

class AfkCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Afk
    }

    override fun declaration() = slashCommand("afk", CommandCategory.SOCIAL, TodoFixThisData) {
        subcommand("on", I18N_PREFIX.On.Description) {
            executor = { AfkOnExecutor(it) }
        }

        subcommand("off", I18N_PREFIX.Off.Description) {
            executor = { AfkOffExecutor(it) }
        }
    }
}