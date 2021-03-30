package net.perfectdreams.loritta.interactions.commands.vanilla

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.commands.SlashCommand
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.CommandManager
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.interactions.LorittaInteractions
import net.perfectdreams.loritta.interactions.internal.commands.DummyMessage
import net.perfectdreams.loritta.interactions.internal.commands.InteraKTionsChannel
import net.perfectdreams.loritta.interactions.internal.commands.InteractionsCommandContext

class InteractionsCommandManager(val m: LorittaInteractions) : CommandManager<LorittaCommand<CommandContext>> {
    val commands = mutableListOf<LorittaCommand<CommandContext>>()

    override fun register(command: LorittaCommand<CommandContext>) {
        commands.add(command)
    }

    override fun unregister(command: LorittaCommand<CommandContext>) {
        commands.remove(command)
    }

    suspend fun registerDiscord() {
        val locale = m.localeManager.getLocaleById("default")

        val interaktionsCommands = commands.map {
            // Convert Loritta Command Type to Discord InteraKTions Command Type
            val declaration = object: SlashCommandDeclaration(
                it.rootDeclaration.name,
                locale.get(it.rootDeclaration.description).substring(0 until 100)
            ) {}

            SlashCommandWrapper(
                it,
                declaration
            )
        }

        for (command in interaktionsCommands) {
            // Trying to register directly causes a "NoClassDefFoundError" because of the anonymous class (but why?)
            m.server.commandManager.register(command)
        }

        m.server.commandManager.updateAllCommandsInGuild(
            Snowflake(297732013006389252L),
            deleteUnknownCommands = true
        )
    }

    inner class SlashCommandWrapper(val command: LorittaCommand<CommandContext>, declaration: SlashCommandDeclaration) : SlashCommand(declaration) {
        override suspend fun executes(context: SlashCommandContext) {
            command.executes(
                InteractionsCommandContext(
                    m,
                    command,
                    DummyMessage(
                        InteraKTionsChannel(
                            context
                        )
                    ),
                    m.localeManager.getLocaleById("default")
                )
            )
        }
    }
}