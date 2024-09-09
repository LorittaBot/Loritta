package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.SonhosExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay.PayExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.sonhosrank.SonhosRankExecutor

class SonhosCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Sonhos
        val CATEGORY_I18N_PREFIX = I18nKeysData.Commands.Category.Economy
        val SONHOS_I18N_PREFIX = I18nKeysData.Commands.Command.Sonhosatm
        val SONHOS_RANK_I18N_PREFIX = I18nKeysData.Commands.Command.Sonhosrank
        val PAY_I18N_PREFIX = I18nKeysData.Commands.Command.Pay
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.ECONOMY, CATEGORY_I18N_PREFIX.RootCommandDescription) {
        subcommand(SONHOS_I18N_PREFIX.Label, SONHOS_I18N_PREFIX.Description) {
            executor = { SonhosExecutor(it) }
        }

        subcommand(PAY_I18N_PREFIX.Label, PAY_I18N_PREFIX.Description) {
            executor = { PayExecutor(it) }
        }

        subcommand(SONHOS_RANK_I18N_PREFIX.Label, SONHOS_RANK_I18N_PREFIX.Description) {
            executor = { SonhosRankExecutor(it) }
        }
    }
}