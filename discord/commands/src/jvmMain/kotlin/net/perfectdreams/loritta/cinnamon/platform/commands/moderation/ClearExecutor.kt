package net.perfectdreams.loritta.cinnamon.platform.commands.moderation

import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.Snowflake
import dev.kord.rest.json.request.BulkDeleteRequest
import dev.kord.rest.route.Position
import dev.kord.rest.service.RestClient
import kotlinx.coroutines.delay
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.URLUtils
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.moderation.declarations.ClearCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class ClearExecutor(val rest: RestClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(ClearExecutor::class) {
        object Options : CommandOptions() {
            val amount = integer("amount", ClearCommand.I18N_PREFIX.Options.Amount)
                .register()

            val keyword = optionalString("keyword", ClearCommand.I18N_PREFIX.Options.Keyword)
                .register()

            val messageType = optionalString("type", ClearCommand.I18N_PREFIX.Options.MessageType)
                .choice("link", ClearCommand.I18N_PREFIX.Options.Choices.Link)
                .choice("attachment", ClearCommand.I18N_PREFIX.Options.Choices.Attachment)
                .register()

            val users = userList("user", ClearCommand.I18N_PREFIX.Options.User, 0, 4)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.failEphemerally {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val amount = args[Options.amount]
        val keyword = args[Options.keyword]
        val messageType = args[Options.messageType]
        val users = args[Options.users].ifEmpty { null }

        if (amount !in 2..1000)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(ClearCommand.I18N_PREFIX.InvalidClearRange),
                    Emotes.Error
                )
            }

        context.deferChannelMessage()

        val channel = rest.channel.getChannel(context.interaKTionsContext.channelId)

        if (channel.lastMessageId == null)
            context.fail {
                styled(
                    context.i18nContext.get(ClearCommand.I18N_PREFIX.NoMessagesToClear),
                    Emotes.Error
                )
            }

        val messages = mutableListOf<List<DiscordMessage>>()

        if (amount <= 100) {
            val messagesToAdd = getMessages(
                channel.id,
                channel.lastMessageId!!.value!!,
                amount
            )?.filter {
                (((System.currentTimeMillis() / 1000) - it.id.timestamp.epochSeconds) < 1209600)
            }

            if (messagesToAdd != null)
                messages.add(messagesToAdd)
        } else {
            var currentAmount = amount
            var lastMessageId = channel.lastMessageId!!.value!!

            while (currentAmount > 0) {
                val messagesToAdd = getMessages(
                    channel.id,
                    lastMessageId,
                    if (currentAmount > 100) 100 else currentAmount
                ) ?: break

                lastMessageId = messagesToAdd.lastOrNull()?.id ?: break
                currentAmount -= messagesToAdd.size

                messages.add(
                    messagesToAdd.filter {
                        (((System.currentTimeMillis() / 1000) - it.id.timestamp.epochSeconds) < 1209600)
                    }
                )

                if (((System.currentTimeMillis() / 1000) - lastMessageId.timestamp.epochSeconds) > 1209600)
                    break

                delay(currentAmount + 900)
            }
        }

        var deletedMessages = 0L

        for (messageGroup in messages) {
            val filteredMessages = messageGroup.filter { message ->
                keyword?.let { message.content.contains(keyword, true) } ?: true
                        && users?.any { it.id == message.author.id } ?: true
                        && messageType?.let {
                    when (it) {
                        "attachment" -> message.attachments.isNotEmpty()
                        "link" -> URLUtils.isValidURL(message.content)
                        else -> true
                    }
                } ?: true
            }

            if (filteredMessages.size < 2)
                context.fail {
                    styled(
                        context.i18nContext.get(ClearCommand.I18N_PREFIX.CouldNotFindMessages),
                        Emotes.Error
                    )
                }

            deletedMessages += filteredMessages.size

            rest.channel.bulkDelete(
                channel.id,
                BulkDeleteRequest(filteredMessages.map { it.id }),
                context.i18nContext.get(
                    ClearCommand.I18N_PREFIX.BulkDeleteReason("${context.user.name}#${context.user.discriminator}")
                )
            )
        }

        context.sendMessage {
            styled(
                context.i18nContext.get(
                    ClearCommand.I18N_PREFIX.ClearSuccess(deletedMessages)
                ),
                Emotes.Tada
            )

            if (amount != deletedMessages)
                styled(
                    context.i18nContext.get(
                        ClearCommand.I18N_PREFIX.SuccessButIgnoredMessages(amount - deletedMessages)
                    ),
                    Emotes.SmallBlueDiamond
                )
        }
    }

    private suspend fun getMessages(channelId: Snowflake, beforeMessageId: Snowflake, limit: Long) =
        rest.channel.getMessages(
            channelId,
            Position.Before(beforeMessageId),
            limit.toInt()
        ).filter { !it.pinned }.ifEmpty { null }
}