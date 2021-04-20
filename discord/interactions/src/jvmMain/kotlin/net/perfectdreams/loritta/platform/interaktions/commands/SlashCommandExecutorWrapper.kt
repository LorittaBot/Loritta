package net.perfectdreams.loritta.platform.interaktions.commands

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.images.URLImageReference
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.interaktions.LorittaInteraKTions
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsMessageChannel
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsUser
import net.perfectdreams.loritta.platform.interaktions.utils.metrics.Prometheus

/**
 * Bridge between Cinnamon's [CommandExecutor] and Discord InteraKTions' [SlashCommandExecutor].
 *
 * Used for argument conversion between the two platforms
 */
class SlashCommandExecutorWrapper(
    private val loritta: LorittaInteraKTions,
    private val locale: BaseLocale,
    // This is only used for metrics
    private val declaration: CommandDeclarationBuilder,
    private val declarationExecutor: CommandExecutorDeclaration,
    private val executor: CommandExecutor,
    private val rootSignature: Int
) : SlashCommandExecutor() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun execute(context: SlashCommandContext, args: SlashCommandArguments) {
        val stringifiedArgumentNames = stringifyArgumentNames(args.types)
        val firstLabel = declaration.labels.first()
        val executorClazzName = executor::class.simpleName

        logger.info { "(${context.user.id.value}) $executor $stringifiedArgumentNames" }

        val timer = Prometheus.EXECUTED_COMMAND_LATENCY_COUNT
            .labels(firstLabel, executorClazzName)
            .startTimer()

        try {
            // Map Cinnamon Arguments to Discord InteraKTions Arguments
            val cinnamonArgs = mutableMapOf<CommandOption<*>, Any?>()
            val interaKTionsArgumentEntries = args.types.entries

            val cinnamonContext = InteraKTionsCommandContext(
                loritta,
                locale,
                InteraKTionsMessageChannel(context)
            )

            declarationExecutor.options.arguments.forEach {
                when (it.type) {
                    is CommandOptionType.StringList -> {
                        // Special case: Lists
                        val listsValues = interaKTionsArgumentEntries.filter { opt -> it.name.startsWith(it.name) }
                        cinnamonArgs[it] = mutableListOf<String>().also {
                            it.addAll(listsValues.map { it.value as String })
                        }
                    }

                    is CommandOptionType.ImageReference -> {
                        // Special case: Image References
                        val imageReferenceArgs =
                            interaKTionsArgumentEntries.filter { opt -> it.name.startsWith(it.name) }

                        var found = false
                        for ((interaKTionOption, value) in imageReferenceArgs) {
                            if (interaKTionOption.name == "${it.name}_avatar" && value != null) {
                                // If the type is a user OR a nullable user, and the value isn't null...
                                val interaKTionUser = value as User

                                // TODO: Animated Avatars?
                                cinnamonArgs[it] = URLImageReference(
                                    "https://cdn.discordapp.com/avatars/${interaKTionUser.id.value}/${interaKTionUser.avatar}.png?size=256"
                                )
                                found = true
                                break
                            }

                            if (interaKTionOption.name == "${it.name}_url" && value != null) {
                                cinnamonArgs[it] = URLImageReference(value as String)
                                found = true
                                break
                            }

                            if (interaKTionOption.name == "${it.name}_emote" && value != null) {
                                val strValue = value as String
                                val emoteId = strValue.substringAfterLast(":").substringBefore(">")
                                cinnamonArgs[it] =
                                    URLImageReference("https://cdn.discordapp.com/emojis/${emoteId}.png?v=1")
                                found = true
                                break
                            }
                        }

                        if (!found) {
                            // TODO: Improve this lol
                            cinnamonContext.sendMessage {
                                content = "tá mas cadê a imagem nn sei"
                                isEphemeral = true
                            }
                            return
                        }
                    }

                    else -> {
                        val interaKTionArgument =
                            interaKTionsArgumentEntries.firstOrNull { opt -> it.name == opt.key.name }

                        // If the value is null but it *wasn't* meant to be null, we are going to throw a exception!
                        // (This should NEVER happen!)
                        if (interaKTionArgument?.value == null && it.type !is CommandOptionType.Nullable)
                            throw UnsupportedOperationException("Argument ${interaKTionArgument?.key} valie is null, but the type of the argument is ${it.type}! Bug?")

                        when (it.type) {
                            is CommandOptionType.User, CommandOptionType.NullableUser -> {
                                cinnamonArgs[it] = interaKTionArgument?.value?.let { InteraKTionsUser(interaKTionArgument.value as User) }
                            }

                            else -> {
                                cinnamonArgs[it] = interaKTionArgument?.value
                            }
                        }
                    }
                }
            }

            GlobalScope.launch {
                delay(2_000)
                if (!context.isDeferred) {
                    logger.warn { "Command $declarationExecutor hasn't been deferred yet! Deferring..." }

                    Prometheus.AUTOMATICALLY_DEFERRED_COUNT
                        .labels(firstLabel, executorClazzName)
                        .inc()

                    context.defer()
                }
            }

            executor.execute(
                cinnamonContext,
                CommandArguments(cinnamonArgs)
            )
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while executing $firstLabel $executorClazzName" }

            // Tell the user that something went *really* wrong
            // We don't have access to the Cinnamon Context (sadly), so we will use the Discord InteraKTions context
            context.sendEphemeralMessage {
                var reply = "${loritta.emotes.loriShrug} **|** " + locale["commands.errorWhileExecutingCommand", loritta.emotes.loriRage, loritta.emotes.loriSob]

                if (!e.message.isNullOrEmpty())
                    // TODO: Sanitize
                    reply += " `${e.message}`"

                content = reply
            }
            return
        }

        val commandLatency = timer.observeDuration()
        logger.info { "(${context.user.id.value}) $executor $stringifiedArgumentNames - OK! Took ${commandLatency * 1000}ms" }
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
    private fun stringifyArgumentNames(types: Map<net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOption<*>, Any?>) = types.map { it.key.name to it.value }
        .toMap()
}