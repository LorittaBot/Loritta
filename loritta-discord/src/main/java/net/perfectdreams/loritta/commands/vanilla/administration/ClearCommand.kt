package net.perfectdreams.loritta.commands.vanilla.administration

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.utils.Constants
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.utils.sendStyledReply
import java.util.*
import java.util.concurrent.TimeUnit

class ClearCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("clean", "limpar", "clear"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.clear.description")
        localizedExamples("commands.moderation.clear.examples")

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
            val channel = discordMessage.channel as? TextChannel ?: return@executesDiscord

            // The message count can't be null or be higher than 500 and lower than 2
            if (count == null || count !in 2..MAX_RANGE)
                fail(locale["commands.moderation.clear.invalidClearRange"], Constants.ERROR)

            // If the guild already have a clear operation queued, we'll prevent them from creating another one
            if (unavailableGuilds.contains(guild.idLong))
                fail(locale["commands.moderation.clear.operationQueued"], Constants.ERROR)

            // The filter text and target user, null if not available
            val (targets, targetInserted, text, textInserted) = getOptions()

            if (targets.isEmpty() && targetInserted)
                fail(locale["commands.moderation.clear.invalidUserFilter"], Constants.ERROR)
            if (text == null && textInserted)
                fail(locale["commands.moderation.clear.invalidTextFilter"], Constants.ERROR)

            val messages = channel.iterableHistory.takeAsync(count).await()

            val allowedMessages = messages.applyAvailabilityFilterToCollection(text, targets)
            val disallowedMessages = messages.minus(allowedMessages)

            if (allowedMessages.isEmpty()) // If there are no allowed messages, we'll cancel the execution
                fail(locale["commands.moderation.clear.couldNotFindMessages"], Constants.ERROR)

            // Deleting the user's message (the command one, +clear)
            runCatching {
                discordMessage.delete()
                        .submit()
                        .await()
            }

            // Clear the messages after deleting the command's one3
            clear(allowedMessages)

            sendStyledReply {
                append {
                    prefix = "\uD83C\uDF89"
                    message = locale["commands.moderation.clear.success", allowedMessages.size, user.asMention]
                }

                appendIf(disallowedMessages.isNotEmpty()) {
                    prefix = "\uD83D\uDD37"
                    message = locale["commands.moderation.clear.successButIgnoredMessages", disallowedMessages.size]
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
        val options = args.drop(1).joinToString("").trim().split("from")

        var text: String? = options.firstOrNull()
        var textInserted = true

        if (text?.trim()?.startsWith("from:") == true) {
            text = null
            textInserted = false
        }

        val targets = mutableSetOf<Long>()
        val targetArguments = options.let { if (text != null) it.drop(text.split(" ").size) else it }

        var targetInserted = false

        for (target in targetArguments) {
            val user = DiscordUtils.extractUserFromString(target, guild = discordMessage.guild)?.idLong

            if (user == null) {
                targets.clear()
                break
            }

            targets.add(user)
            targetInserted = true
        }

        return CommandOptions(targets, targetInserted, text, textInserted)
    }

    /**
     * This will clear the provided [messages] using the "purge" function,
     * and will register the operation too.
     *
     * @param messages The messages that should be deleted
     */
    private fun DiscordCommandContext.clear(messages: List<Message>) {
        unavailableGuilds.add(guild.idLong) // Adding the operation to the guild
        discordMessage.textChannel.purgeMessages(messages) // Purging the messages
    }

    data class CommandOptions(
            val targets: Set<Long>,
            val targetInserted: Boolean,
            val text: String?,
            val textInserted: Boolean
    )

    companion object {

        const val MAX_RANGE = 1000L

        const val TARGET_OPTION_NAME = "from"
        const val TEXT_FILTERING_OPTION_NAME = "contains"

        @JvmStatic
        private val unavailableGuilds = Collections.newSetFromMap(Caffeine.newBuilder()
                .expireAfterWrite(MAX_RANGE / 100, TimeUnit.SECONDS)
                .build<Long, Boolean>().asMap()
        )

    }

}