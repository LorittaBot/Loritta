package net.perfectdreams.loritta.platform.cli

import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.vanilla.PingCommandExecutor
import net.perfectdreams.loritta.platform.cli.entities.CLIMessageChannel

suspend fun main() {
    val command = PingCommandExecutor()
    command.execute(CommandContext(CLIMessageChannel()),)
}