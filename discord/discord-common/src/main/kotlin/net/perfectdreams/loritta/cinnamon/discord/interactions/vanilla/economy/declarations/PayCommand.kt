package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay.PayExecutor
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

class PayCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Pay
    }

    override fun declaration() = slashCommand("pay", CommandCategory.ECONOMY, I18N_PREFIX.Description) {
        executor = { PayExecutor(it) }
    }
}