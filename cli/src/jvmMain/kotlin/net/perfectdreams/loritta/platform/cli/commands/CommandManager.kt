package net.perfectdreams.loritta.platform.cli.commands

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.images.URLImageReference
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.platform.cli.LorittaCLI
import net.perfectdreams.loritta.platform.cli.entities.CLIMessageChannel

class CommandManager(val loritta: LorittaCLI) {
    companion object {
        private const val ARGUMENT_PREFIX = "--"
    }

    val declarations = mutableListOf<CommandDeclarationBuilder>()
    val executors = mutableListOf<CommandExecutor>()

    fun register(declaration: CommandDeclaration, vararg executors: CommandExecutor) {
        declarations.add(declaration.declaration())
        this.executors.addAll(executors)
    }

    private fun parseArgs(args: List<String>, commandArgs: List<CommandOption<*>>): MutableMap<CommandOption<*>, Any?> {
        val argsResults = mutableMapOf<CommandOption<*>, Any?>()

        var currentArgumentName: String? = null
        val currentArgumentValue = mutableListOf<String>()

        fun addArgument() {
            if (currentArgumentName != null) {
                // Store current argument
                val option = commandArgs.firstOrNull { it.name == currentArgumentName }
                    ?: throw RuntimeException("Trying to find a argument that doesn't exist!")
                when (option.type) {
                    is CommandOptionType.StringList -> {
                        // Special case: Lists
                        // We can input multiple arguments for a list, so we are going to get the stored list in the map and use it!
                        val list = argsResults.getOrPut(option) { mutableListOf<String>() } as MutableList<String>
                        list.add(currentArgumentValue.joinToString(" "))
                    }
                    is CommandOptionType.String, CommandOptionType.NullableString -> argsResults[option] =
                        currentArgumentValue.joinToString(" ")
                    is CommandOptionType.Integer -> argsResults[option] =
                        currentArgumentValue.joinToString(" ").toInt()
                    is CommandOptionType.NullableInteger -> argsResults[option] =
                        currentArgumentValue.joinToString(" ").toIntOrNull()
                    is CommandOptionType.ImageReference -> argsResults[option] =
                        URLImageReference(currentArgumentValue.joinToString(" "))

                    else -> throw UnsupportedOperationException("I don't know how to convert ${option.type}!")
                }
            }
        }

        for (arg in args) {
            if (arg.startsWith(ARGUMENT_PREFIX)) {
                if (currentArgumentName != null)
                    addArgument()

                currentArgumentValue.clear()
                currentArgumentName = arg.removePrefix(ARGUMENT_PREFIX)
            } else if (currentArgumentName != null)
                currentArgumentValue.add(arg)
        }

        addArgument()

        return argsResults
    }

    suspend fun matches(content: String): Boolean {
        val split = content.split(" ")
        var firstIndexOfArgument = split.indexOfFirst { it.startsWith(ARGUMENT_PREFIX) }
        if (firstIndexOfArgument == -1)
            firstIndexOfArgument = split.size

        // Everything is a label until we find a "--" (which indicates that it is the start of a argument)
        val commandSplit = split.take(firstIndexOfArgument)

        for (declaration in declarations) {
            val matchedDeclaration = getLabelsConnectedToCommandDeclaration(
                commandSplit,
                declaration
            ) ?: continue

            val executor = executors.first {
                it::class == matchedDeclaration.executor?.parent
            }

            val argumentsSplit = split.drop(firstIndexOfArgument)

            val args = parseArgs(argumentsSplit, matchedDeclaration.executor?.options?.arguments ?: listOf())

            executor.execute(
                CLICommandContext(
                    loritta,
                    loritta.localeManager.getLocaleById("default"),
                    CLIMessageChannel()
                ),
                CommandArguments(args)
            )
            return true
        }

        return false
    }

    // This part of the code is lifted from how Discord InteraKTions handles Discord Interactions commands
    // However there is a difference: We can't check if it is a subcommand group or a subcommand within CLI
    /**
     * Checks if the [labels] are connected from the [rootDeclaration] to the [declaration], by checking the [rootDeclaration] and its children until
     * the [declaration] is found.
     *
     * @param labels          the request labels in order
     * @param rootDeclaration the root declaration
     * @param declaration     the declaration that must be found
     * @return the matched declaration
     */
    fun getLabelsConnectedToCommandDeclaration(labels: List<String>, declaration: CommandDeclarationBuilder): CommandDeclarationBuilder? {
        // Let's not over complicate this, we already know that Discord only supports one level deep of nesting
        // (so group -> subcommand)
        // So let's do easy and quick checks
        val firstLabel = labels.first()
        if (declaration.labels.any { it == firstLabel }) {
            // Matches the root label! Yay!
            if (labels.size == 1) {
                // If there is only a Root Label, then it means we found our root declaration!
                return declaration
            } else {
                if (labels.size == 2) {
                    val secondLabel = labels[1]

                    // If not, let's check subcommands
                    for (subcommand in declaration.subcommands) {
                        if (subcommand.labels.any { it == secondLabel }) {
                            // Matches, then return this!
                            return subcommand
                        }
                    }
                } else if (labels.size == 3) {
                    val secondLabel = labels[1]
                    val thirdLabel = labels[1]

                    // If not, let's check subcommand groups and subcommands
                    for (group in declaration.subcommandGroups) {
                        if (group.labels.any { it == secondLabel }) {
                            for (subcommand in group.subcommands) {
                                if (subcommand.labels.any { it == thirdLabel }) {
                                    // Matches, then return this!
                                    return subcommand
                                }
                            }
                        }
                    }
                }
                // Nothing found, return...
                return null
            }
        }
        return null
    }
}