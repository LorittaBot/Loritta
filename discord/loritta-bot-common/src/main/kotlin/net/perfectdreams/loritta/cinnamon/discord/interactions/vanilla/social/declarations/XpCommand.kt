package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.ViewXpExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank.XpRankExecutor
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.common.utils.TodoFixThisData

class XpCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val XP_VIEW_I18N_PREFIX = I18nKeysData.Commands.Command.Xpview
        val XP_RANK_I18N_PREFIX = I18nKeysData.Commands.Command.Xprank
        val I18N_PREFIX = I18nKeysData.Commands.Command.Xp
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.SOCIAL, TodoFixThisData) {
        dmPermission = false

        subcommand(XP_VIEW_I18N_PREFIX.Label, XP_VIEW_I18N_PREFIX.Description) {
            executor = { ViewXpExecutor(it) }
        }

        subcommand(XP_RANK_I18N_PREFIX.Label, XP_RANK_I18N_PREFIX.Description) {
            executor = { XpRankExecutor(it) }
        }
    }
}