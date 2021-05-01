package net.perfectdreams.loritta.platform.twitter.commands

import blue.starry.penicillin.endpoints.account
import blue.starry.penicillin.endpoints.statuses
import blue.starry.penicillin.endpoints.statuses.show
import blue.starry.penicillin.endpoints.users
import blue.starry.penicillin.endpoints.users.showByUserId
import blue.starry.penicillin.models.Status
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.images.URLImageReference
import net.perfectdreams.loritta.platform.twitter.LorittaTwitter
import net.perfectdreams.loritta.platform.twitter.entities.TwitterMessageChannel
import net.perfectdreams.loritta.platform.twitter.entities.TwitterUser

class CommandManager(val loritta: LorittaTwitter) {
    companion object {
        private const val ARGUMENT_PREFIX = "--"
    }

    val declarations = mutableListOf<CommandDeclarationBuilder>()
    val executors = mutableListOf<CommandExecutor>()

    fun register(declaration: CommandDeclaration, vararg executors: CommandExecutor) {
        declarations.add(declaration.declaration())
        this.executors.addAll(executors)
    }

    private suspend fun parseArgs(status: Status, args: List<String>, commandArgs: List<CommandOption<*>>): MutableMap<CommandOption<*>, Any?> {
        val editedArgs = args.toMutableList()
        val argsResults = mutableMapOf<CommandOption<*>, Any?>()

        // We will match without the argument name, following the argument register order
        commandArgs.forEach {
            val arg = editedArgs.getOrNull(0)

            println("Option: $arg $it")

            when {
                arg == null && it is CommandOptionType.Nullable -> {
                    // Just ignore, no issues ^-^
                }

                it.type is CommandOptionType.String -> argsResults[it] = arg

                it.type is CommandOptionType.ImageReference -> {
                    // Image Reference, we will try to "figure out" what image to use
                    if (arg?.startsWith("@") == true) {
                        // If it starts with a @, then it may be a screen name!
                        val mentionedUser = status.entities.userMentions.firstOrNull {
                            it.screenName == arg.removePrefix("@")
                        }

                        if (mentionedUser != null) {
                            val user = loritta.client.users.showByUserId(mentionedUser.id).execute()
                            argsResults[it] = URLImageReference(user.result.profileImageUrlHttps)
                        }
                    } else {
                        val inReplyToStatusId = status.inReplyToStatusId

                        println("in reply to status id: $inReplyToStatusId")
                        var getSelfMediaIndex = 0
                        var getReplyMediaIndex: Int? = null

                        if (arg?.startsWith("self") == true) {
                            getSelfMediaIndex = (arg.removePrefix("self").toIntOrNull()?.coerceIn(1..4) ?: 1) - 1
                        }
                        if (arg?.startsWith("reply") == true) {
                            getReplyMediaIndex = (arg.removePrefix("reply").toIntOrNull()?.coerceIn(1..4) ?: 1) - 1
                        }

                        val media = status.entities.media.getOrNull(getSelfMediaIndex)

                        // If there is a media, use it in the command!
                        if (media != null) {
                            argsResults[it] = URLImageReference(media.mediaUrl)
                        } else if (inReplyToStatusId != null) {
                            val inReplyToStatus = loritta.client.statuses.show(inReplyToStatusId)
                                .execute()

                            println(inReplyToStatus)

                            val inReplyToStatusMedia =
                                inReplyToStatus.result.entities.media.getOrNull(getReplyMediaIndex ?: 0)

                            println("status: $inReplyToStatusMedia")

                            if (inReplyToStatusMedia != null)
                                argsResults[it] = URLImageReference(inReplyToStatusMedia.mediaUrl)
                        }
                    }
                }
            }

            if (arg != null) // Only remove if the arg is not null, to avoid errors
            editedArgs.removeAt(0)
        }

        return argsResults
    }

    suspend fun matches(status: Status, content: String): Boolean {
        val split = content.split(" ")

        for (declaration in declarations) {
            val matchedCommand = getLabelsConnectedToCommandDeclaration(
                split,
                declaration
            ) ?: continue

            val (matchedDeclaration, arguments) = matchedCommand

            val executor = executors.first {
                it::class == matchedDeclaration.executor?.parent
            }

            println("Argument: $arguments")
            val args = parseArgs(status, arguments, matchedDeclaration.executor?.options?.arguments ?: listOf())

            executor.execute(
                TwitterCommandContext(
                    loritta,
                    loritta.localeManager.getLocaleById("default"),
                    TwitterUser(),
                    TwitterMessageChannel(loritta.client, status)
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
     * @return the matched declaration and the arguments
     */
    fun getLabelsConnectedToCommandDeclaration(labels: List<String>, declaration: CommandDeclarationBuilder): Pair<CommandDeclarationBuilder, List<String>>? {
        // Let's not over complicate this, we already know that Discord only supports one level deep of nesting
        // (so group -> subcommand)
        // So let's do easy and quick checks
        val firstLabel = labels.first()
        if (declaration.labels.any { it == firstLabel }) {
            // Matches the root label! Yay!
            // Let's check subcommand and subcommand groups!!
            val secondLabel = labels.getOrNull(1)
            val thirdLabel = labels.getOrNull(2)

            // If not, let's check subcommand groups and subcommands
            if (secondLabel != null && thirdLabel != null) {
                for (group in declaration.subcommandGroups) {
                    if (group.labels.any { it == secondLabel }) {
                        for (subcommand in group.subcommands) {
                            if (subcommand.labels.any { it == thirdLabel }) {
                                // Matches, then return this!
                                return Pair(subcommand, labels.drop(3))
                            }
                        }
                    }
                }
            }

            // If not, let's check subcommands
            if (secondLabel != null) {
                for (subcommand in declaration.subcommands) {
                    if (subcommand.labels.any { it == secondLabel }) {
                        // Matches, then return this!
                        return Pair(subcommand, labels.drop(2))
                    }
                }
            }

            // If not, it is the root declaration
            return Pair(declaration, labels.drop(1))
        }
        return null
    }
}