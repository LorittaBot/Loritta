package net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.SonhosExecutor

object SonhosCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Sonhos

    override fun declaration() = slashCommand(listOf("sonhos", "atm"), CommandCategory.ECONOMY, I18N_PREFIX.Description) {
        executor = SonhosExecutor
    }
}