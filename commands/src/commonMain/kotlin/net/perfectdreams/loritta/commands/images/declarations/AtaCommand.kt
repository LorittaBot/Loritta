package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.MonicaAtaExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object AtaCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.art"

    override fun declaration() = command(listOf("ata")) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommand(listOf("monica")) {
            description = LocaleKeyData("commands.command.ata.description")
            executor = MonicaAtaExecutor
        }
    }
}