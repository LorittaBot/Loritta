package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.CoinFlipExecutor

object CoinFlipCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Coinflip

    override fun declaration() = slashCommand(listOf("coinflip", "girarmoeda", "flipcoin", "caracoroa"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = CoinFlipExecutor
    }
}