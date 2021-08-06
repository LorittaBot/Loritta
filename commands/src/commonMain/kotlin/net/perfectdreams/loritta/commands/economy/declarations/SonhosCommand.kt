package net.perfectdreams.loritta.commands.economy.declarations

import net.perfectdreams.loritta.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object SonhosCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.sonhos"

    override fun declaration() = command(listOf("sonhos", "atm"), CommandCategory.ECONOMY, LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
        executor = SonhosExecutor
    }
}