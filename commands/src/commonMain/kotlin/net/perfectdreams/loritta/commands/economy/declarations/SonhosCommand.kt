package net.perfectdreams.loritta.commands.economy.declarations

import net.perfectdreams.loritta.commands.economy.SonhosExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object SonhosCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.sonhos"

    override fun declaration() = command(listOf("sonhos", "atm"), CommandCategory.ECONOMY) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = SonhosExecutor
    }
}