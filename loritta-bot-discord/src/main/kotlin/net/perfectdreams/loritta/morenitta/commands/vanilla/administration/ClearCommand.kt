package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.loritta.morenitta.utils.Constants
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.await
import kotlinx.coroutines.joinAll
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.sendStyledReply
import java.util.*
import java.util.concurrent.TimeUnit

class ClearCommand(loritta: LorittaBot): DiscordAbstractCommandBase(loritta, listOf("clean", "limpar", "clear"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.command.clear.description")
        localizedExamples("commands.command.clear.examples")

        userRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)
        botRequiredPermissions = listOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)

        usage {
            argument(ArgumentType.NUMBER) {
                optional = false
            }
            argument(ArgumentType.TEXT){
                optional = true
            }
        }

        executesDiscord {
            if (args.isEmpty()) return@executesDiscord explain()

            val count = args[0].toIntOrNull()
            val channel = discordMessage.channel as? GuildMessageChannel ?: return@executesDiscord

            // The message count can't be null or be higher than 500 and lower than 2
            if (count == null || count !in 2..MAX_RANGE)
                fail(locale["commands.command.clear.invalidClearRange"], Constants.ERROR)

            // If the guild already have a clear operation queued, we'll prevent them from creating another one
            if (unavailableGuilds.contains(guild.idLong))
                fail(locale["commands.command.clear.operationQueued"], Constants.ERROR)

            // The filter text and target user, null if not available
            val (targets, text, textInserted) = getOptions()

            if (targets.filterNotNull().isEmpty() && targets.isNotEmpty())
                fail(locale["commands.command.clear.invalidUserFilter"], Constants.ERROR)
            if (text == null && textInserted)
                fail(locale["commands.command.clear.invalidTextFilter"], Constants.ERROR)

            // Deleting the user's message (the command one, +clear)
            runCatching {
                discordMessage.delete()
                        .submit()
                        .await()
            }

            val messages = channel.iterableHistory.takeAsync(count).await()

            val allowedMessages = messages.applyAvailabilityFilterToCollection(text, targets.filterNotNull().toSet()).minus(discordMessage)
            val disallowedMessages = messages.minus(allowedMessages)

            if (allowedMessages.isEmpty()) // If there are no allowed messages, we'll cancel the execution
                fail(locale["commands.command.clear.couldNotFindMessages"], Constants.ERROR)

            // Clear the messages after deleting the command's one3
            clear(allowedMessages)

            sendStyledReply {
                append {
                    prefix = "\uD83C\uDF89"
                    message = locale["commands.command.clear.success", allowedMessages.size, user.asMention]
                }

                appendIf(disallowedMessages.isNotEmpty()) {
                    prefix = "\uD83D\uDD37"
                    message = locale["commands.command.clear.successButIgnoredMessages", disallowedMessages.size]
                }
            }
        }
    }

    /**
     * Filter the messages that should be deleted or not,
     * the factors are the ones bellow, only those messages will be deleted.
     *
     * @see command
     *
     * @factor The message can't be older than 2 weeks
     * @factor The message can't be pinned
     * @factor If the target isn't null, the message must be from the target
     * @factor If the text isn't null, the message must contains the text
     */
    private fun List<Message>.applyAvailabilityFilterToCollection(text: String?, targets: Set<Long>) = filter {
        (((System.currentTimeMillis() / 1000) - it.timeCreated.toEpochSecond()) < 1209600) // The message can't be older than 2 weeks
                && (it.isPinned.not()) // The message can't be pinned
                && (if (targets.isNotEmpty()) targets.contains(it.author.idLong) else true) // If the target isn't null, the message must be from one of the targets
                && (if (text != null) it.contentStripped.contains(text.trim(), ignoreCase = true) else true) // If the text isn't null, the message must contains the text
    }

    /**
     * This method will retrieve all the 
     * command options to the user, including the contains and from one
     * 
     * @return Command options
     */
    private suspend fun DiscordCommandContext.getOptions(): CommandOptions {
        val optionName = locale["commands.command.clear.targetOption"]
        val options = args.drop(1).joinToString(" ").trim().split("$optionName:")

        var text: String? = options.firstOrNull()
        var textInserted = true

        if (text?.trim()?.startsWith("$optionName:") == true) {
            text = null
            textInserted = false
        }

        val targetArguments = options.let { if (text != null) it.drop(text.split(" ").size) else it }
        val targets = getUserIdsFromArguments(guild, targetArguments)

        return CommandOptions(targets, text, textInserted)
    }

    /**
     * This method will try to get the max number of users
     * possible, and if one of them is invalid, it will return an empty list
     * to provide better validating/debugging
     *
     * @param guild The message's guild
     * @param arguments The arguments that will be checked
     * @return Null if at least one target was inserted but not found
     */
    private suspend fun getUserIdsFromArguments(guild: Guild?, arguments: List<String>): Set<Long?> {
        val targets: MutableSet<Long?> = mutableSetOf()
        for (target in arguments) {
            targets.add(DiscordUtils.extractUserFromString(loritta, target.trim(), guild = guild)?.idLong)
        }
        return targets
    }

    /**
     * This will clear the provided [messages] using the "purge" function,
     * and will register the operation too.
     *
     * @param messages The messages that should be deleted
     */
    private suspend fun DiscordCommandContext.clear(messages: List<Message>) {
        unavailableGuilds.add(guild.idLong) // Adding the operation to the guild
        discordMessage.guildChannel.purgeMessages(messages)
                .map { it.asDeferred() }.joinAll() // Purging the messages and awaiting

        unavailableGuilds.remove(guild.idLong)
    }

    data class CommandOptions(
            val targets: Set<Long?>,
            val text: String?,
            val textInserted: Boolean
    )

    companion object {

        const val MAX_RANGE = 1000L

        @JvmStatic
        private val unavailableGuilds = Collections.newSetFromMap(Caffeine.newBuilder()
                .expireAfterWrite(MAX_RANGE / 100, TimeUnit.SECONDS)
                .build<Long, Boolean>().asMap()
        )

    }

}