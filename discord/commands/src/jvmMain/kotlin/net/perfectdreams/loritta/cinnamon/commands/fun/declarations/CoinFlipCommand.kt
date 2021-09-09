package net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object CoinFlipCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Coinflip

    override fun declaration() = command(listOf("coinflip", "girarmoeda", "flipcoin", "caracoroa"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = CoinFlipExecutor
    }
}