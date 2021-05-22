package net.perfectdreams.loritta.platform.discord.commands

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import mu.KotlinLogging
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandException
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.entities.JDAMessageChannel
import net.perfectdreams.loritta.platform.discord.entities.JDAUser

class JDACommandManager(val loritta: LorittaDiscord) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val declarations = mutableListOf<CommandDeclarationBuilder>()
    val executors = mutableListOf<CommandExecutor>()

    fun register(declaration: CommandDeclaration, vararg executors: CommandExecutor) {
        declarations.add(declaration.declaration())
        this.executors.addAll(executors)
    }

    suspend fun matches(
        content: LorittaMessageEvent,
        rawArguments: MutableList<String>
    ): Boolean {
        // Everything is a label right now, so we need to process and try to find the best match for us
        // This makes the code be more complex, with slash commands we just need to check the amount of labels and match accordingly
        // But because we don't have this glamour over the gateway, we need to check everything manually (and that sucks!)
        for (declaration in declarations) {
            // This is a hack!! A gigantic workaround!!
            // I hate this!! But it works!!! :3
            // What we will do is keep looping all arguments (up to three, which is the maximum supported by the getLabelsConnectedToCommandDeclaration function)
            // and the best match will be chosen to be executed
            var bestMatchedDeclaration: CommandDeclarationBuilder? = null
            var bestMatchedRequiresHowManyLabels = 0

            repeat(3) {
                val matchedDeclaration = getLabelsConnectedToCommandDeclaration(
                    // So we are going to get from 1 to 3
                    // 1 = Root Declaration
                    // 2 = Root Declaration + Subcommand
                    // 3 = Root Declaration + Subcommand Group + Subcommand
                    rawArguments.take(it + 1),
                    declaration
                )

                if (matchedDeclaration != null) {
                    bestMatchedDeclaration = matchedDeclaration
                    bestMatchedRequiresHowManyLabels = it + 1
                }
            }

            val matchedDeclaration = bestMatchedDeclaration ?: continue
            val howManyLabels = bestMatchedRequiresHowManyLabels

            val executor = executors.first {
                it::class == matchedDeclaration.executor?.parent
            }

            val argumentsSplit = rawArguments.drop(howManyLabels)

            val args = parseArgs(argumentsSplit, matchedDeclaration.executor?.options?.arguments ?: listOf())

            val context = JDACommandContext(
                loritta,
                loritta.localeManager.getLocaleById("default"),
                JDAUser(content.author),
                JDAMessageChannel(content.channel)
            )

            try {
                executor.execute(
                    context,
                    CommandArguments(args)
                )
            } catch (e: CommandException) {
                context.channel.sendMessage(e.lorittaMessage)
            }
            return true
        }

        return false
    }

    private fun parseArgs(args: List<String>, commandArgs: List<CommandOption<*>>): MutableMap<CommandOption<*>, Any?> {
        val argsResults = mutableMapOf<CommandOption<*>, Any?>()

        for (commandArgument in commandArgs) {
            if (commandArgument.type == CommandOptionType.String) {
                argsResults[commandArgument] = args.joinToString(" ")
            }
        }

        return argsResults
    }

    // This part of the code is lifted from how Discord InteraKTions handles Discord Interactions commands
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
                    val thirdLabel = labels[2]

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