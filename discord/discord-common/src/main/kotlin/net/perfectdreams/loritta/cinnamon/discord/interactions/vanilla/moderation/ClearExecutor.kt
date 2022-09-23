package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.ClearCommand
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import kotlin.time.Duration.Companion.days

class ClearExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val amount = integer("amount", ClearCommand.I18N_PREFIX.Options.Amount) {
            maxValue = 10000
            minValue = 2
        }

        val keyword = optionalString("keyword", ClearCommand.I18N_PREFIX.Options.Keyword) {
            maxLength = 4
        }

        val messageType = optionalString("message_type", ClearCommand.I18N_PREFIX.Options.MessageType) {
            choice(ClearCommand.I18N_PREFIX.Options.Choices.Attachment, "attachment")
        }

        val users = optionalString("users", ClearCommand.I18N_PREFIX.Options.User)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context as GuildApplicationCommandContext

        context.deferChannelMessageEphemerally()

        val channel = loritta.kord.getChannelOf<GuildMessageChannel>(context.channelId)

        if (channel?.lastMessageId == null)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(ClearCommand.I18N_PREFIX.NoMessagesToClear),
                    Emotes.Error
                )
            }

        val amount = args[options.amount].toInt()
        val messagesChunk = filterMessages(args, context, getMessages(channel, amount))?.chunked(100)
            ?: context.failEphemerally {
                styled(
                    context.i18nContext.get(ClearCommand.I18N_PREFIX.CouldNotFindMessages),
                    Emotes.Error
                )
            }

        var deletedMessagesSize = 0

        for (messages in messagesChunk) {
            bulkDeleteMessages(channel, context, messages)
            deletedMessagesSize += messages.size
        }

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(
                    ClearCommand.I18N_PREFIX.ClearSuccess(deletedMessagesSize)
                ),
                Emotes.Tada
            )

            if (amount != deletedMessagesSize)
                styled(
                    context.i18nContext.get(
                        ClearCommand.I18N_PREFIX.SuccessButIgnoredMessages(amount - deletedMessagesSize)
                    ),
                    Emotes.SmallBlueDiamond
                )
        }
    }

    private suspend fun filterMessages(
        args: SlashCommandArguments,
        context: GuildApplicationCommandContext,
        messages: List<Message>
    ): List<Message>? {
        val keyword = args[options.keyword]
        val messageType = args[options.messageType]
        val users = args[options.users]?.let {
            AdminUtils.checkAndRetrieveAllValidUsersFromString(
                context,
                it
            )
        }

        return messages.filter { message ->
            message.canBeDeleted() &&
                    keyword?.let { message.content.contains(keyword, true) } ?: true
                    && users?.any { it.user.id == message.author?.id } ?: true
                    && messageType?.let {
                when (it) {
                    "attachment" -> message.attachments.isNotEmpty()
                    else -> true
                }
            } ?: true
        }.ifEmpty { null }
    }

    private suspend fun bulkDeleteMessages(
        channel: GuildMessageChannel,
        context: GuildApplicationCommandContext,
        messages: List<Message>
    ) {
        val reason = context.i18nContext.get(ClearCommand.I18N_PREFIX.BulkDeleteReason(context.user.tag))

        if (messages.size < 2) {
            messages.forEach { channel.deleteMessage(it.id, reason) }
        } else {
            channel.bulkDelete(messages.map { it.id }, reason)
        }
    }

    private suspend fun getMessages(channel: GuildMessageChannel, amount: Int) =
        channel.getMessagesBefore(
            Snowflake.max,
            amount
        ).toList()

    private fun Message.canBeDeleted(): Boolean =
        timestamp > (Clock.System.now() - 14.days) && !isPinned
}