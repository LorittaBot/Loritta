package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.ViewXpExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank.XpRankExecutor
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData

class XpCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val XP_VIEW_I18N_PREFIX = I18nKeysData.Commands.Command.Xpview
        val XP_RANK_I18N_PREFIX = I18nKeysData.Commands.Command.Xprank
    }

    override fun declaration() = slashCommand("xp", CommandCategory.SOCIAL, TodoFixThisData) {
        dmPermission = false

        subcommand("view", XP_VIEW_I18N_PREFIX.Description) {
            executor = { ViewXpExecutor(it) }
        }

        subcommand("rank", XP_RANK_I18N_PREFIX.Description) {
            executor = { XpRankExecutor(it) }
        }
    }
}