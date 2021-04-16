package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.EdnaldoBandeiraExecutor
import net.perfectdreams.loritta.commands.images.EdnaldoTvExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object EdnaldoCommand : CommandDeclaration {
    override fun declaration() = command(listOf("ednaldo")) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommand(listOf("bandeira", "flag")) {
            description = LocaleKeyData("commands.command.ednaldobandeira.description")
            executor = EdnaldoBandeiraExecutor
        }

        subcommand(listOf("tv")) {
            description = LocaleKeyData("commands.command.ednaldotv.description")
            executor = EdnaldoTvExecutor
        }
    }
}