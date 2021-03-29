package net.perfectdreams.loritta.interactions.commands.vanilla

import net.perfectdreams.discordinteraktions.commands.SlashCommand
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration

class PingCommand : SlashCommand(this) {
    companion object : SlashCommandDeclaration(
        name = "ping",
        description = "Pong!"
    )

    override suspend fun executes(context: SlashCommandContext) {
        context.sendMessage {
            content = "Pong!"
        }
    }
}