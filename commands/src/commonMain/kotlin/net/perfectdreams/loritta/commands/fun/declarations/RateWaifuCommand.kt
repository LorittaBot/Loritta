package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object RateWaifuCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.ratewaifu"

    override fun declaration() = command(listOf("ratewaifu", "avaliarwaifu", "ratemywaifu", "avaliarminhawaifu", "notawaifu")) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = RateWaifuExecutor
    }
}