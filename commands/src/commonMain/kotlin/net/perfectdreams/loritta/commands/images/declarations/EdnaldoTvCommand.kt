package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object EdnaldoTvCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.ednaldotv"

    override fun declaration() = command(listOf("ednaldotv")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = EdnaldoTvExecutor
    }
}