package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.SonhosExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay.PayExecutor
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData

class SonhosCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val CATEGORY_I18N_PREFIX = I18nKeysData.Commands.Category.Economy
        val SONHOS_I18N_PREFIX = I18nKeysData.Commands.Command.Sonhos
        val PAY_I18N_PREFIX = I18nKeysData.Commands.Command.Pay
    }

    override fun declaration() = slashCommand("sonhos", CommandCategory.ECONOMY, CATEGORY_I18N_PREFIX.RootCommandDescription) {
        subcommand("atm", SONHOS_I18N_PREFIX.Description) {
            executor = { SonhosExecutor(it) }
        }

        subcommand("pay", PAY_I18N_PREFIX.Description) {
            executor = { PayExecutor(it) }
        }
    }
}