package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.ChicoAtaExecutor
import net.perfectdreams.loritta.commands.images.GessyAtaExecutor
import net.perfectdreams.loritta.commands.images.LoriAtaExecutor
import net.perfectdreams.loritta.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object AtaCommand : CommandDeclaration {
    override fun declaration() = command(listOf("ata")) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommand(listOf("monica")) {
            description = LocaleKeyData("commands.command.ata.description")
            executor = MonicaAtaExecutor
        }

        subcommand(listOf("chico")) {
            description = LocaleKeyData("commands.command.chicoata.description")
            executor = ChicoAtaExecutor
        }

        subcommand(listOf("lori")) {
            description = LocaleKeyData("commands.command.loriata.description")
            executor = LoriAtaExecutor
        }

        subcommand(listOf("gessy")) {
            description = LocaleKeyData("commands.command.gessyata.description")
            executor = GessyAtaExecutor
        }
    }
}