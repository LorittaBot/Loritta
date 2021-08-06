package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object RateWaifuCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.ratewaifu"

    override fun declaration() = command(listOf("ratewaifu", "avaliarwaifu", "ratemywaifu", "avaliarminhawaifu", "notawaifu"), CommandCategory.FUN, LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
        executor = RateWaifuExecutor
    }
}