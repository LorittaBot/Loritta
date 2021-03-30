package net.perfectdreams.loritta.interactions.commands.vanilla

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

class PingCommand : LorittaCommand<CommandContext>(this) {
    companion object : CommandDeclaration(
        name = "ping",
        description = LocaleKeyData("commands.missingDescription")
    )

    override suspend fun executes(context: CommandContext) {
        context.sendMessage("Pong!")
    }
}