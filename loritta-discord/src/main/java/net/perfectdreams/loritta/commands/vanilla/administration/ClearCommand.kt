package net.perfectdreams.loritta.commands.vanilla.administration

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.extensions.build
import net.perfectdreams.loritta.utils.extensions.toJDA
import net.perfectdreams.loritta.utils.styledReply
import java.util.*
import java.util.concurrent.CompletableFuture
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
            argument(ArgumentType.USER){
                optional = true
            }
        }

        executesDiscord {
            if (args.isEmpty()) return@executesDiscord explain()

            val count = args[0].toIntOrNull()
            val channel = discordMessage.channel as? TextChannel ?: return@executesDiscord

            // The message count can't be null or be higher than 500 and lower than 2
            if (count == null || count !in 2..500)
                fail(locale["commands.moderation.clear.invalidClearRange"], Constants.ERROR)

            // If the guild already have a clear operation queued, we'll prevent them from creating another one
            if (unavailableGuilds.contains(guild.idLong))
                fail(locale["commands.moderation.clear.operationQueued"], Constants.ERROR)

            // The filter text, null if not available
            val text = args.drop(1).joinToString(" ").split("|").lastOrNull()?.trim()

            // Deleting the user's message (the command one, +clear)
            runCatching {
                discordMessage.delete()
                        .submit()
                        .await()
            }

            val messages = channel.iterableHistory.takeAsync(count).await()

            val allowedMessages = messages.applyAvailabilityFilterToCollection(text, user(1))
            val disallowedMessages = messages.minus(allowedMessages)

            if (allowedMessages.isEmpty()) // If there are no allowed messages, we'll cancel the execution
                fail(locale["commands.moderation.clear.couldNotFindMessages"], Constants.ERROR)
            else clear(allowedMessages) // But if so, we're going to clear them!

            styledReply {
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
    private fun List<Message>.applyAvailabilityFilterToCollection(text: String?, target: JDAUser?) = filter {
        (((System.currentTimeMillis() / 1000) - it.timeCreated.toEpochSecond()) < 1209600) // The message can't be older than 2 weeks
                && (it.isPinned.not()) // The message can't be pinned
                && (if (target != null) it.author.idLong == target.id else true) // If the target isn't null, the message must be from the target
                && (if (text != null) it.contentStripped.contains(text.trim(), ignoreCase = true) else true) // If the text isn't null, the message must contains the text
    }

    private fun DiscordCommandContext.sendMessagesToAuditLog(messages: List<Message>) {
        val channelId = serverConfig.eventLogConfig?.eventLogChannelId ?: return
        val channel = guild.getTextChannelById(channelId) ?: return

        channel.sendMessage("").queue()
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

    companion object {

        @JvmStatic
        private val unavailableGuilds = Collections.newSetFromMap(Caffeine.newBuilder()
                .expireAfterAccess(4, TimeUnit.SECONDS)
                .build<Long, Boolean>().asMap()
        )

    }

}