package net.perfectdreams.loritta.cinnamon.discord.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.slash.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.context.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.context.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.context.commands.slash.SlashCommandArguments
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.entities.ServerConfigRoot
import net.perfectdreams.loritta.cinnamon.common.images.URLImageReference
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOption
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptionType
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.Prometheus
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import kotlin.streams.toList

/**
 * Bridge between Cinnamon's [CommandExecutor] and Discord InteraKTions' [SlashCommandExecutor].
 *
 * Used for argument conversion between the two platforms
 */
class SlashCommandExecutorWrapper(
    private val loritta: LorittaCinnamon,
    // This is only used for metrics
    private val rootDeclaration: CommandDeclarationBuilder,
    private val declarationExecutor: CommandExecutorDeclaration,
    private val executor: CommandExecutor,
    private val rootSignature: Int
) : SlashCommandExecutor() {
    companion object {
        private val logger = KotlinLogging.logger {}

        private val SUPPORTED_IMAGE_EXTENSIONS = listOf(
            "png",
            "jpg",
            "jpeg",
            "bmp",
            "tiff",
            "gif"
        )
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val stringifiedArgumentNames = stringifyArgumentNames(args.types)
        val rootDeclarationClazzName = rootDeclaration.parent.simpleName
        val executorClazzName = executor::class.simpleName

        logger.info { "(${context.sender.id.value}) $executor $stringifiedArgumentNames" }

        val timer = Prometheus.EXECUTED_COMMAND_LATENCY_COUNT
            .labels(rootDeclarationClazzName, executorClazzName)
            .startTimer()

        // These variables are used in the catch { ... } block, to make our lives easier
        var i18nContext: I18nContext? = null
        var cinnamonContext: CommandContext? = null

        try {
            // Map Cinnamon Arguments to Discord InteraKTions Arguments
            val cinnamonArgs = mutableMapOf<CommandOption<*>, Any?>()
            val interaKTionsArgumentEntries = args.types.entries

            val guildId = if (context is GuildApplicationCommandContext) {
                context.guildId
            } else {
                null
            }

            val serverConfig = if (guildId != null) {
                loritta.services.serverConfigs.getServerConfigRootById(guildId.value) ?: NonGuildServerConfigRoot( // Fallback to a fake guild config if it doesn't exist
                    guildId.value,
                    "pt" // Default to Portuguese
                )
            } else {
                // TODO: Should this class *really* be named "ServerConfig"? After all, it isn't always used for guilds
                NonGuildServerConfigRoot(
                    0u,
                    "pt" // Default to Portuguese
                )
            }

            // Patches and workarounds!!!
            val localeId = when (serverConfig.localeId) {
                "default" -> "pt"
                "en-us" -> "en"
                else -> serverConfig.localeId
            }

            i18nContext = loritta.languageManager.getI18nContextById(localeId)

            // val channel = loritta.interactions.rest.channel.getChannel(context.request.channelId)

            cinnamonContext = CommandContext(
                loritta,
                i18nContext,
                context.sender,
                context
            )

            if (!rootDeclaration.allowedInPrivateChannel && guildId == null) {
                TODO()
                /* context.sendEphemeralMessage {
                    content = ":no_entry: **|** ${locale["commands.cantUseInPrivate"]}"
                } */
                return
            }

            declarationExecutor.options.arguments.forEach {
                when (it.type) {
                    is CommandOptionType.StringList -> {
                        // Special case: Lists
                        val listsValues = interaKTionsArgumentEntries.filter { opt -> opt.key.name.startsWith(it.name) }
                        cinnamonArgs[it] = mutableListOf<String>().also {
                            it.addAll(listsValues.map { it.value as String })
                        }
                    }

                    is CommandOptionType.ImageReference -> {
                        // Special case: Image References
                        // Get the argument that matches our image reference
                        val interaKTionArgument = interaKTionsArgumentEntries.firstOrNull { opt -> opt.key.name == it.name }
                        var found = false

                        if (interaKTionArgument != null) {
                            val value = interaKTionArgument.value as String

                            // Now check if it is a valid thing!
                            // First, we will try matching via user mentions
                            if (value.startsWith("<@") && value.endsWith(">")) {
                                // Maybe it is a mention?
                                val userId = value
                                    .removePrefix("<@")
                                    .removePrefix("!") // User has a nickname
                                    .removeSuffix(">")
                                    .toLongOrNull()

                                if (userId != null) {
                                    val user = context.data.resolved?.users?.get(Snowflake(userId))

                                    if (user != null) {
                                        // User avatar found! Let's use it!!
                                        cinnamonArgs[it] = URLImageReference(user.avatar.url)
                                        found = true
                                    }
                                }
                            }

                            if (!found && value.startsWith("http")) {
                                // It is a URL!
                                // TODO: Use a RegEx to check if it is a valid URL
                                cinnamonArgs[it] = URLImageReference(value)
                                found = true
                            }

                            if (!found) {
                                // It is a emote!
                                // Discord emotes always starts with "<" and ends with ">"
                                if (value.startsWith("<") && value.endsWith(">")) {
                                    val emoteId = value.substringAfterLast(":").substringBefore(">")
                                    cinnamonArgs[it] =
                                        URLImageReference("https://cdn.discordapp.com/emojis/${emoteId}.png?v=1")
                                } else {
                                    // If not, we are going to handle it as if it were a Unicode emoji
                                    val emoteId = value.codePoints().toList()
                                        .joinToString(separator = "-") { String.format("\\u%04x", it).substring(2) }
                                    cinnamonArgs[it] =
                                        URLImageReference("https://twemoji.maxcdn.com/2/72x72/$emoteId.png")
                                }
                                found = true
                            }
                        }

                        // TODO: Fix this, removed for now
                        /* if (interaKTionOption.name == "${it.name}_history" && value != null) {
                            val boolValue = value as Boolean

                            if (boolValue) {
                                TODO()
                                // If true, we are going to find the first recent message in this chat
                                /* val channelId = context.request.channelId
                                val messages = loritta.rest.channel.getMessages(
                                    channelId,
                                    null,
                                    100
                                )

                                try {
                                    // Sort from the newest message to the oldest message
                                    val attachmentUrl = messages.sortedByDescending { it.id.timeStamp }
                                        .flatMap { it.attachments }
                                        .firstOrNull {
                                            // Only get filenames ending with "image" extensions
                                            it.filename.substringAfter(".")
                                                .toLowerCase() in SUPPORTED_IMAGE_EXTENSIONS
                                        }?.url

                                    if (attachmentUrl != null) {
                                        cinnamonArgs[it] = URLImageReference(attachmentUrl)
                                        found = true
                                    }
                                } catch (e: Exception) {
                                    // TODO: Catch the "permission required" exception and show a nice message
                                    e.printStackTrace()
                                } */
                            }
                            break
                        } */

                        if (!found)
                            cinnamonContext.fail(cinnamonContext.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.loriSob)
                    }

                    else -> {
                        val interaKTionArgument = interaKTionsArgumentEntries.firstOrNull { opt -> it.name == opt.key.name }

                        // If the value is null but it *wasn't* meant to be null, we are going to throw a exception!
                        // (This should NEVER happen!)
                        if (interaKTionArgument?.value == null && it.type !is CommandOptionType.Nullable)
                            throw UnsupportedOperationException("Argument ${interaKTionArgument?.key} valie is null, but the type of the argument is ${it.type}! Bug?")

                        when (it.type) {
                            is CommandOptionType.User, CommandOptionType.NullableUser -> {
                                cinnamonArgs[it] = interaKTionArgument?.value?.let {
                                    interaKTionArgument.value
                                }
                            }

                            is CommandOptionType.Channel, CommandOptionType.NullableChannel -> {
                                println("okay, tipo de canal")
                                println("Argumento é null? ${interaKTionArgument == null}")
                                println("Guild é null? ${guildId == null}")
                                // cinnamonArgs[it] = interaKTionArgument?.value?.let { guild?.toLorittaGuild(loritta.interactions.rest)?.retrieveChannel((interaKTionArgument.value as Channel).id.value) }
                                TODO()
                            }

                            else -> {
                                cinnamonArgs[it] = interaKTionArgument?.value
                            }
                        }
                    }
                }
            }

            executor.execute(
                cinnamonContext,
                CommandArguments(cinnamonArgs)
            )
        } catch (e: Throwable) {
            if (e is SilentCommandException)
                return // SilentCommandExceptions should be ignored

            if (e is CommandException) {
                context.sendMessage(e.builder)
                return
            }

            if (e is EphemeralCommandException) {
                context.sendEphemeralMessage(e.builder)
                return
            }

            logger.warn(e) { "Something went wrong while executing $rootDeclarationClazzName $executorClazzName" }

            // If the i18nContext is not present, we will default to the default language provided
            i18nContext = i18nContext ?: loritta.languageManager.getI18nContextById(loritta.languageManager.defaultLanguageId)

            // Tell the user that something went *really* wrong
            // While we do have access to the Cinnamon Context, it may be null at this stage, so we will use the Discord InteraKTions context
            val content = "${Emotes.loriHm} **|** " + i18nContext.get(
                I18nKeysData.Commands.ErrorWhileExecutingCommand(
                    loriRage = Emotes.loriRage,
                    loriSob = Emotes.loriSob,
                    stacktrace = if (!e.message.isNullOrEmpty())
                        " `${e.message}`" // TODO: Sanitize
                    else
                        ""
                )
            )

            // If the context was already deferred, but it isn't ephemeral, then we will send a non-ephemeral message
            val isEphemeral = if (context.isDeferred)
                context.wasInitiallyDeferredEphemerally
            else true


            if (isEphemeral)
                context.sendEphemeralMessage {
                    this.content = content
                }
            else
                context.sendMessage {
                    this.content = content
                }
        }

        val commandLatency = timer.observeDuration()
        logger.info { "(${context.sender.id.value}) $executor $stringifiedArgumentNames - OK! Took ${commandLatency * 1000}ms" }
    }

    override fun signature() = rootSignature

    /**
     * Stringifies the arguments in the [types] map to its name
     *
     * Useful for debug logging
     *
     * @param types the arguments
     * @return a map with argument name -> argument value
     */
    private fun stringifyArgumentNames(types: Map<net.perfectdreams.discordinteraktions.declarations.commands.slash.options.CommandOption<*>, Any?>) = types.map { it.key.name to it.value }
        .toMap()

    class NonGuildServerConfigRoot(
        override val id: ULong,
        override val localeId: String
    ) : ServerConfigRoot
}