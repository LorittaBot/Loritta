package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import kotlin.streams.toList
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.commands.slash.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.context.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.context.commands.GuildApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.context.commands.slash.SlashCommandArguments
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.images.ImageReference
import net.perfectdreams.loritta.cinnamon.common.images.URLImageReference
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOption
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptionType
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.Prometheus
import net.perfectdreams.loritta.cinnamon.pudding.data.ServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import kotlin.streams.toList
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext as CinnamonApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext as CinnamonGuildApplicationCommandContext

/**
 * Bridge between Cinnamon's [CommandExecutor] and Discord InteraKTions' [SlashCommandExecutor].
 *
 * Used for argument conversion between the two platforms
 */
class SlashCommandExecutorWrapper(
    private val loritta: LorittaCinnamon,
    // This is only used for metrics and logs
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

        val NonGuildServerConfigRoot = ServerConfigRoot(0u, "pt")
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val stringifiedArgumentNames = stringifyArgumentNames(args.types)
        val rootDeclarationClazzName = rootDeclaration.parent.simpleName
        val executorClazzName = executor::class.simpleName

        logger.info { "(${context.sender.id.value}) $executor $stringifiedArgumentNames" }

        val timer = Prometheus.EXECUTED_COMMAND_LATENCY_COUNT
            .labels(rootDeclarationClazzName, executorClazzName)
            .startTimer()

        // Map Cinnamon Arguments to Discord InteraKTions Arguments
        val cinnamonArgs = mutableMapOf<CommandOption<*>, Any?>()
        val guildId = (context as? GuildApplicationCommandContext)?.guildId

        val result = executeCommand(
            rootDeclarationClazzName,
            executorClazzName,
            context,
            args,
            cinnamonArgs,
            guildId
        )

        var stacktrace: String? = null
        if (result is CommandExecutionFailure)
            stacktrace = result.throwable.stackTraceToString()

        val commandLatency = timer.observeDuration()
        logger.info { "(${context.sender.id.value}) $executor $stringifiedArgumentNames - OK! Result: ${result}; Took ${commandLatency * 1000}ms" }

        loritta.services.executedApplicationCommandsLog.insertApplicationCommandLog(
            context.sender.id.value.toLong(),
            guildId?.value?.toLong(),
            context.channelId.value.toLong(),
            Clock.System.now(),
            ApplicationCommandType.CHAT_INPUT,
            rootDeclarationClazzName!!,
            executorClazzName!!,
            buildJsonWithArguments(cinnamonArgs),
            stacktrace == null,
            commandLatency,
            stacktrace
        )
    }

    private suspend fun executeCommand(
        rootDeclarationClazzName: String?,
        executorClazzName: String?,
        context: ApplicationCommandContext,
        args: SlashCommandArguments,
        cinnamonArgs: MutableMap<CommandOption<*>, Any?>,
        guildId: Snowflake?
    ): CommandExecutionResult {
        // These variables are used in the catch { ... } block, to make our lives easier
        var i18nContext: I18nContext? = null
        val cinnamonContext: CinnamonApplicationCommandContext?

        val interaKTionsArgumentEntries = args.types.entries

        try {
            val serverConfig = if (guildId != null) {
                // TODO: Fix this workaround, while this does work, it isn't that good
                loritta.services.servers.getServerConfigRoot(guildId.value)?.data ?: NonGuildServerConfigRoot
            } else {
                // TODO: Should this class *really* be named "ServerConfig"? After all, it isn't always used for guilds
                NonGuildServerConfigRoot
            }

            // Patches and workarounds!!!
            val localeId = when (serverConfig.localeId) {
                "default" -> "pt"
                "en-us" -> "en"
                else -> serverConfig.localeId
            }

            i18nContext = loritta.languageManager.getI18nContextById(localeId)

            cinnamonContext = if (context is GuildApplicationCommandContext) {
                CinnamonGuildApplicationCommandContext(
                    loritta,
                    i18nContext,
                    context.sender,
                    context,
                    context.guildId,
                    context.member
                )
            } else {
                CinnamonApplicationCommandContext(
                    loritta,
                    i18nContext,
                    context.sender,
                    context
                )
            }

            // Don't let users that are banned from using Loritta
            if (handleIfBanned(cinnamonContext))
                return CommandExecutionSuccess

            if (!rootDeclaration.allowedInPrivateChannel && guildId == null) {
                TODO()
                /* context.sendEphemeralMessage {
                    content = ":no_entry: **|** ${locale["commands.cantUseInPrivate"]}"
                } */
                return CommandExecutionSuccess
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

                    is CommandOptionType.UserList -> {
                        val listsValues = interaKTionsArgumentEntries.filter { opt -> opt.key.name.startsWith(it.name) }
                        cinnamonArgs[it] = mutableListOf<User>().also {
                            it.addAll(listsValues.map { it.value as User })
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

                        if (!found) {
                            // If no image was found, we will try to find the first recent message in this chat
                            val channelId = context.channelId
                            val messages = loritta.rest.channel.getMessages(
                                channelId,
                                null,
                                100
                            )
                            try {
                                // Sort from the newest message to the oldest message
                                val attachmentUrl = messages.sortedByDescending { it.id.timestamp }
                                    .flatMap { it.attachments }
                                    .firstOrNull {
                                        // Only get filenames ending with "image" extensions
                                        it.filename.substringAfter(".")
                                            .lowercase() in SUPPORTED_IMAGE_EXTENSIONS
                                    }?.url

                                if (attachmentUrl != null) {
                                    // Found a valid URL, let's go!
                                    cinnamonArgs[it] = URLImageReference(attachmentUrl)
                                    found = true
                                }
                            } catch (e: Exception) {
                                // TODO: Catch the "permission required" exception and show a nice message
                                e.printStackTrace()
                            }
                        }

                        if (!found)
                            cinnamonContext.fail(cinnamonContext.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
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
                                cinnamonArgs[it] = interaKTionArgument?.value?.let {
                                    interaKTionArgument.value
                                }
                            }

                            is CommandOptionType.Role, CommandOptionType.NullableRole -> {
                                cinnamonArgs[it] = interaKTionArgument?.value?.let {
                                    interaKTionArgument.value
                                }
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

            return CommandExecutionSuccess
        } catch (e: Throwable) {
            if (e is SilentCommandException)
                return CommandExecutionSuccess // SilentCommandExceptions should be ignored

            if (e is CommandException) {
                context.sendMessage(e.builder)
                return CommandExecutionSuccess
            }

            if (e is EphemeralCommandException) {
                context.sendEphemeralMessage(e.builder)
                return CommandExecutionSuccess
            }

            logger.warn(e) { "Something went wrong while executing $rootDeclarationClazzName $executorClazzName" }

            // If the i18nContext is not present, we will default to the default language provided
            i18nContext = i18nContext ?: loritta.languageManager.getI18nContextById(loritta.languageManager.defaultLanguageId)

            // Tell the user that something went *really* wrong
            // While we do have access to the Cinnamon Context, it may be null at this stage, so we will use the Discord InteraKTions context
            val content = "${Emotes.LoriHm} **|** " + i18nContext.get(
                I18nKeysData.Commands.ErrorWhileExecutingCommand(
                    loriRage = Emotes.LoriRage,
                    loriSob = Emotes.LoriSob,
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

            return CommandExecutionFailure(e)
        }
    }

    private suspend fun handleIfBanned(context: CinnamonApplicationCommandContext): Boolean {
        // Check if the user is banned from using Loritta
        val userBannedState = loritta.services.users.getUserBannedState(UserId(context.user.id.value))

        if (userBannedState != null) {
            val banDateInEpochSeconds = userBannedState.bannedAt.epochSeconds
            val expiresDateInEpochSeconds = userBannedState.expiresAt?.epochSeconds

            context.sendEphemeralMessage {
                val banAppealPageUrl = loritta.config.website + "extras/faq-loritta/loritta-ban-appeal"
                content = context.i18nContext.get(
                    if (expiresDateInEpochSeconds != null) {
                        I18nKeysData.Commands.YouAreLorittaBannedTemporary(
                            loriHmpf = Emotes.LoriHmpf,
                            reason = userBannedState.reason,
                            banDate = "<t:$banDateInEpochSeconds:R> (<t:$banDateInEpochSeconds:f>)",
                            expiresDate = "<t:$expiresDateInEpochSeconds:R> (<t:$expiresDateInEpochSeconds:f>)",
                            banAppealPageUrl = banAppealPageUrl,
                            loriAmeno = Emotes.loriAmeno,
                            loriSob = Emotes.LoriSob
                        )
                    } else {
                        I18nKeysData.Commands.YouAreLorittaBannedPermanent(
                            loriHmpf = Emotes.LoriHmpf,
                            reason = userBannedState.reason,
                            banDate = "<t:$banDateInEpochSeconds:R> (<t:$banDateInEpochSeconds:f>)",
                            banAppealPageUrl = banAppealPageUrl,
                            loriAmeno = Emotes.loriAmeno,
                            loriSob = Emotes.LoriSob
                        )
                    }

                ).joinToString("\n")
            }
            return true
        }
        return false
    }

    sealed class CommandExecutionResult
    object CommandExecutionSuccess : CommandExecutionResult()
    class CommandExecutionFailure(val throwable: Throwable) : CommandExecutionResult()

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

    private fun buildJsonWithArguments(types: Map<CommandOption<*>, Any?>) = buildJsonObject {
        for ((option, value) in types) {
            when (value) {
                is ImageReference -> put(option.name, value.url)
                is Number -> put(option.name, value)
                else -> put(option.name, value.toString())
            }
        }
    }
}