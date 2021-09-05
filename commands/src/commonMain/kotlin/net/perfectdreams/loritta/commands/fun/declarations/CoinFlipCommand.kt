package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object CoinFlipCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Coinflip

    override fun declaration() = command(listOf("coinflip", "girarmoeda", "flipcoin", "caracoroa"), CommandCategory.FUN, I18N_PREFIX.Description) {
        executor = CoinFlipExecutor
    }
}