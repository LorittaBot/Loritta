package net.perfectdreams.loritta.common.commands.vanilla.declarations

import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.commands.vanilla.PingAyayaCommandExecutor
import net.perfectdreams.loritta.common.commands.vanilla.PingCommandExecutor
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object PingCommandDeclaration {
    fun declaration() = command(listOf("ping")) {
        description = LocaleKeyData("commands.command.ping.description")
        executor = PingCommandExecutor

        subcommand(listOf("ayaya")) {
            executor = PingAyayaCommandExecutor
        }
    }
}