package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.SAMExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object SAMCommand : CommandDeclaration {
    override fun declaration() = command(listOf("sam")) {
        description = LocaleKeyData("commands.command.sam.description")
        executor = SAMExecutor
    }
}