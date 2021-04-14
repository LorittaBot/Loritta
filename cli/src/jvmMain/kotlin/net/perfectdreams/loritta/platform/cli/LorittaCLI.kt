package net.perfectdreams.loritta.platform.cli

import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.commands.misc.declarations.PingCommand
import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.cli.commands.CLICommandContext
import net.perfectdreams.loritta.platform.cli.entities.CLIMessageChannel

class LorittaCLI(val args: Array<String>) : LorittaBot() {
    val commandManager = CommandManager()

    suspend fun start() {
        commandManager.register(
            PingCommand,
            PingExecutor(),
            PingAyayaExecutor()
        )

        commandManager.register(
            CoinFlipCommand,
            CoinFlipExecutor()
        )

        for (declaration in commandManager.declarations) {
            if (executeCommand(args.joinToString(" "), declaration))
                return
        }

        println("No matching command found! Exiting...")
    }

    fun parseArgs(content: String, args: List<CommandOption<*>>): MutableMap<CommandOption<*>, Any?> {
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

    suspend fun executeCommand(content: String, declaration: CommandDeclarationBuilder): Boolean {
        val split = content.split(" ")

        val label = declaration.labels.first()

        if (label == split[0]) {
            for (subcommand in declaration.subcommands) {
                if (executeCommand(split.drop(1).joinToString(" "), subcommand))
                    return true
            }

            val executor = commandManager.executors.first {
                it::class == declaration.executor?.parent
            }

            val args = parseArgs(split.drop(1).joinToString(" "), declaration.executor?.options?.arguments ?: listOf())

            executor.execute(
                CLICommandContext(
                    this,
                    BaseLocale("default", mapOf(), mapOf()),
                    CLIMessageChannel()
                ),
                CommandArguments(args)
            )
            return true
        }

        return false
    }
}