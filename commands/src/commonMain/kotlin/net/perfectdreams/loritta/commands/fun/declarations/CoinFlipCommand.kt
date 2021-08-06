package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object CoinFlipCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.flipcoin"

    override fun declaration() = command(listOf("coinflip", "girarmoeda", "flipcoin", "caracoroa"), CommandCategory.FUN, LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
        executor = CoinFlipExecutor
    }
}