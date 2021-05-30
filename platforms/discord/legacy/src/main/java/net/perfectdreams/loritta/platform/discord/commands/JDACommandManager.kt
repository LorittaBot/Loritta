package net.perfectdreams.loritta.platform.discord.commands

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.referenceIfPossible
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandException
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.entities.JDAMessage
import net.perfectdreams.loritta.platform.discord.entities.JDAMessageChannel
import net.perfectdreams.loritta.platform.discord.entities.JDAUser
import net.perfectdreams.loritta.utils.CommandUtils

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
        event: LorittaMessageEvent,
        rawArguments: MutableList<String>,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        lorittaUser: LorittaUser
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

                // We need to check if it is different than our old declaration because, if we don't, stuff like
                // "/morse to aaaaaa"
                // Will be recognized even if the current it is 3, and that should not happen!
                if (matchedDeclaration != null && matchedDeclaration != bestMatchedDeclaration) {
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

            return processCommand(event, matchedDeclaration, executor, argumentsSplit, serverConfig, locale, lorittaUser)
        }

        return false
    }

    private suspend fun processCommand(
        event: LorittaMessageEvent,
        declaration: CommandDeclarationBuilder,
        executor: CommandExecutor,
        argumentsAsString: List<String>,
        serverConfig: ServerConfig,
        locale: BaseLocale,
        lorittaUser: LorittaUser
    ): Boolean {
        val start = System.currentTimeMillis()

        val args = parseArgs(argumentsAsString, declaration.executor?.options?.arguments ?: listOf())

        val context = JDACommandContext(
            loritta,
            locale,
            JDAUser(event.author),
            JDAMessage(event.message),
            JDAMessageChannel(event.channel),
            lorittaUser
        )

        CommandUtils.logMessageEvent(event, logger)

        try {
            // Check if user is banned
            if (LorittaUtilsKotlin.handleIfBanned(context, lorittaUser.profile))
                return true

            // TODO: Cooldown

            if (serverConfig.blacklistedChannels.contains(event.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
                if (serverConfig.warnIfBlacklisted) {
                    if (serverConfig.blacklistedChannels.isNotEmpty() && event.guild != null && event.member != null && event.textChannel != null) {
                        val generatedMessage = MessageUtils.generateMessage(
                            serverConfig.blacklistedWarning ?: "???",
                            listOf(event.member, event.textChannel),
                            event.guild
                        )
                        if (generatedMessage != null)
                            event.textChannel.sendMessage(generatedMessage)
                                .referenceIfPossible(event.message, serverConfig, true)
                                .await()
                    }
                }
                return true // Ignorar canais bloqueados (return true = fast break, se está bloqueado o canal no primeiro comando que for executado, os outros obviamente também estarão)
            }
            // TODO: Command feedback
            // TODO: Check disabled commands
            // TODO: Check default permissions
            // TODO: Check Loritta Permissions
            // TODO: Help easter egg when using a command with shrug
            // TODO: Owner only command check
            // TODO: canUseCommand
            // TODO: Can't use command in private channel check
            // TODO: Check file upload permission
            // TODO: Donation messages that... aren't being used for donation messages anymore
            // TODO: Inappropriate words check
            // TODO: Guild Owner banned check
            // TODO: Log command to database

            lorittaShards.updateCachedUserData(event.author)

            executor.execute(
                context,
                CommandArguments(args)
            )

            // TODO: Delete user message
            // TODO: Log Prometheus

            val commandLatency = System.currentTimeMillis() - start
            CommandUtils.logMessageEventComplete(event, logger, commandLatency)
        } catch (e: CommandException) {
            context.channel.sendMessage(e.lorittaMessage)
        }
        return true
    }

    /**
     * Parses the arguments in the [args] list, matching the provided arguments from the command in the list [commandArgs]
     *
     * @param args        the user's input arguments
     * @param commandArgs the command's arguments
     * @return a map matching the command arguments to user inputs
     */
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
    private fun getLabelsConnectedToCommandDeclaration(labels: List<String>, declaration: CommandDeclarationBuilder): CommandDeclarationBuilder? {
        // Let's not over complicate this, we already know that Discord only supports one level deep of nesting
        // (so group -> subcommand)
        // So let's do easy and quick checks
        val firstLabel = labels.first().toLowerCase() // Allow using "+CoMaNd"
        if (declaration.labels.any { it.equals(firstLabel, true) }) {
            // Matches the root label! Yay!
            if (labels.size == 1) {
                // If there is only a Root Label, then it means we found our root declaration!
                return declaration
            } else {
                if (labels.size == 2) {
                    val secondLabel = labels[1]

                    // If not, let's check subcommands
                    for (subcommand in declaration.subcommands) {
                        if (subcommand.labels.any { it.equals(secondLabel, true) }) {
                            // Matches, then return this!
                            return subcommand
                        }
                    }
                } else if (labels.size == 3) {
                    val secondLabel = labels[1]
                    val thirdLabel = labels[2]

                    // If not, let's check subcommand groups and subcommands
                    for (group in declaration.subcommandGroups) {
                        if (group.labels.any { it.equals(secondLabel, true) }) {
                            for (subcommand in group.subcommands) {
                                if (subcommand.labels.any { it.equals(thirdLabel, true) }) {
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