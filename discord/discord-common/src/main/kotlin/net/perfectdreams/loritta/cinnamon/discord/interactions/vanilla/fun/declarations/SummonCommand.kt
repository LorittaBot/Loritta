package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.BemBoladaExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.FaustaoExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.TioDoPaveExecutor

class SummonCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Summon
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.FUN, TodoFixThisData) {
        subcommand(I18N_PREFIX.Tiodopave.Label, I18N_PREFIX.Tiodopave.Description) {
            executor = { TioDoPaveExecutor(it) }
        }

        subcommand(I18N_PREFIX.Faustao.Label, I18N_PREFIX.Faustao.Description) {
            executor = { FaustaoExecutor(it) }
        }

        subcommand(I18N_PREFIX.Kenji.Label, I18N_PREFIX.Kenji.Description) {
            executor = { BemBoladaExecutor(it) }
        }
    }
}