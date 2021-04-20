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
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.images.URLImageReference
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.interaktions.LorittaInteraKTions
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsMessageChannel

/**
 * Bridge between Cinnamon's [CommandExecutor] and Discord InteraKTions' [SlashCommandExecutor].
 *
 * Used for argument conversion between the two platforms
 */
class SlashCommandExecutorWrapper(
    val loritta: LorittaInteraKTions,
    val locale: BaseLocale,
    val declarationExecutor: CommandExecutorDeclaration,
    val executor: CommandExecutor,
    val rootSignature: Int
) : SlashCommandExecutor() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun execute(context: SlashCommandContext, args: SlashCommandArguments) {
        val stringifiedArgumentNames = stringifyArgumentNames(args.types)

        logger.info { "(${context.user.id.value}) $executor $stringifiedArgumentNames" }
        val start = System.currentTimeMillis()

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
                    val imageReferenceArgs = interaKTionsArgumentEntries.filter { opt -> it.name.startsWith(it.name) }

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
                            cinnamonArgs[it] = URLImageReference("https://cdn.discordapp.com/emojis/${emoteId}.png?v=1")
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

                    cinnamonArgs[it] = interaKTionArgument?.value
                }
            }
        }

        GlobalScope.launch {
            delay(2_000)
            if (!context.isDeferred) {
                logger.warn { "Command $declarationExecutor hasn't been deferred yet! Deferring..." }
                context.defer()
            }
        }

        executor.execute(
            cinnamonContext,
            CommandArguments(cinnamonArgs)
        )

        val commandLatency = System.currentTimeMillis() - start
        logger.info { "(${context.user.id.value}) $executor $stringifiedArgumentNames - OK! Took ${commandLatency}ms" }
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