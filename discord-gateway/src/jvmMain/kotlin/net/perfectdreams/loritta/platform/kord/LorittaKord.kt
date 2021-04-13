package net.perfectdreams.loritta.platform.kord

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.options.CommandOptionBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.commands.vanilla.PingAyayaCommandExecutor
import net.perfectdreams.loritta.common.commands.vanilla.declarations.PingCommandDeclaration
import net.perfectdreams.loritta.common.commands.vanilla.PingCommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.platform.kord.entities.KordMessageChannel
import java.io.File

class LorittaKord : LorittaBot() {
    val commandManager = CommandManager()

    fun parseArgs(content: String, args: List<CommandOption<*>>): MutableMap<CommandOption<*>, Any?> {
        // --ayaya_count=5
        val regex = Regex("--([A-z_]+)=([A-z0-9_]+)")
        val matchedArgs = regex.findAll(content)

        val argsResults = mutableMapOf<CommandOption<*>, Any?>()

        matchedArgs.forEach {
            val name = it.groupValues[1]
            val value = it.groupValues[2]

            val result = args.firstOrNull { it.name == name } ?: return@forEach

            val convertedArgument = when (result.type) {
                is CommandOptionType.Integer -> value.toInt()
                is CommandOptionType.NullableInteger -> value.toIntOrNull()
                else -> throw UnsupportedOperationException("I don't know how to convert ${result.type}!")
            }

            argsResults[result] = convertedArgument
        }

        return argsResults
    }

    suspend fun executeCommand(event: MessageCreateEvent, content: String, declaration: CommandDeclarationBuilder): Boolean {
        val split = content.split(" ")

        val label = declaration.labels.first()

        if (label == split[0]) {
            for (subcommand in declaration.subcommands) {
                if (executeCommand(event, split.drop(1).joinToString(" "), subcommand))
                    return true
            }

            val executor = commandManager.executors.first {
                it::class == declaration.executor?.parent
            }

            val args = parseArgs(split.drop(1).joinToString(" "), declaration.executor?.options?.arguments ?: listOf())

            executor.execute(
                CommandContext(
                    KordMessageChannel(event.message.getChannel())
                ),
                CommandArguments(args)
            )
            return true
        }

        return false
    }

    fun start() {
        commandManager.register(
            PingCommandDeclaration,
            PingCommandExecutor(),
            PingAyayaCommandExecutor()
        )

        runBlocking {
            val client = Kord(File("token.txt").readText())

            client.on<MessageCreateEvent> {
                try {
                    if (this.message.content.startsWith("!!")) {
                        val contentWithoutPrefix = this.message.content.removePrefix("!!")

                        for (declaration in commandManager.declarations) {
                            if (executeCommand(this, contentWithoutPrefix, declaration))
                                return@on
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            client.login()
        }
    }
}