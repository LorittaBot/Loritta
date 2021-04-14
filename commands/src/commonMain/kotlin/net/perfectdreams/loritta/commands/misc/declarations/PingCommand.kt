package net.perfectdreams.loritta.commands.misc.declarations

import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object PingCommand : CommandDeclaration {
    override fun declaration() = command(listOf("ping")) {
        description = LocaleKeyData("commands.command.ping.description")
        executor = PingExecutor

        subcommand(listOf("ayaya")) {
            description = LocaleKeyData("commands.command.ping.description")

            executor = PingAyayaExecutor
        }
    }
}