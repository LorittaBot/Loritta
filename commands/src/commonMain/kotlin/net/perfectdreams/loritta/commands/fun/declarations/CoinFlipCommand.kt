package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object CoinFlipCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.flipcoin"

    override fun declaration() = command(listOf("coinflip", "girarmoeda", "flipcoin", "caracoroa"), CommandCategory.FUN) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = CoinFlipExecutor
    }
}