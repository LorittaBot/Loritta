package net.perfectdreams.loritta.interactions.commands.vanilla

import kotlinx.coroutines.delay
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand

class PingAnotherThingCommand : LorittaCommand<CommandContext>(PingCommandDeclaration.AnotherThing, PingCommandDeclaration) {
    override suspend fun executes(context: CommandContext) {
        delay(5_000)

        context.sendMessage("Pong???")
    }
}