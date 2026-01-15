package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.generics.getChannel
import kotlinx.html.emptyMap
import kotlinx.serialization.json.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.WebhookClient
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.URLUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.WebhookUtils
import java.util.*

class WebhookCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Webhook
    }

    val messageUrlRegex = Regex("/channels/([0-9]+)/([0-9]+)/([0-9]+)")

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.DISCORD, UUID.fromString("222022f9-caa0-4f3a-9147-e048302582e4")) {
        subcommandGroup(I18N_PREFIX.Send.Label, TodoFixThisData) {
            subcommand(I18N_PREFIX.Send.Simple.Label, I18N_PREFIX.Send.Simple.Description, UUID.fromString("13899a72-b1bb-40bb-9a55-d90162d97f0d")) {
                executor = WebhookSendSimpleExecutor()
            }

            subcommand(I18N_PREFIX.Send.Json.Label, I18N_PREFIX.Send.Json.Description, UUID.fromString("d8005136-e18b-4f40-9847-05944f007c5b")) {
                executor = WebhookSendJsonExecutor()
            }

            subcommand(I18N_PREFIX.Send.Repost.Label, I18N_PREFIX.Send.Repost.Description, UUID.fromString("b7df9e7c-1ea7-4541-8663-2612c73a4ed2")) {
                executor = WebhookSendRepostExecutor()
            }
        }

        subcommandGroup(I18N_PREFIX.Edit.Label, TodoFixThisData) {
            /* subcommand(I18N_PREFIX.Edit.Simple.Label, I18N_PREFIX.Edit.Simple.Description) {
                executor = WebhookEditSimpleExecutor(it)
            } */

            subcommand(I18N_PREFIX.Edit.Json.Label, I18N_PREFIX.Edit.Json.Description, UUID.fromString("8cc0820c-b585-43b7-8006-328f3f74f194")) {
                executor = WebhookEditJsonExecutor()
            }

            /* subcommand(I18N_PREFIX.Edit.Repost.Label, I18N_PREFIX.Send.Repost.Description) {
                executor = WebhookEditRepostExecutor(it)
            } */
        }
    }

    inner class WebhookSendSimpleExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)

            val message = optionalString("message", WebhookCommand.I18N_PREFIX.Options.Message.Text)

            val username = optionalString("username", WebhookCommand.I18N_PREFIX.Options.Username.Text)

            val avatarUrl = optionalString("avatar_url", WebhookCommand.I18N_PREFIX.Options.AvatarUrl.Text)

            val embedTitle = optionalString("embed_title", WebhookCommand.I18N_PREFIX.Options.EmbedTitle.Text)

            val embedDescription = optionalString("embed_description", WebhookCommand.I18N_PREFIX.Options.EmbedDescription.Text)

            val embedImageUrl = optionalString("embed_image_url", WebhookCommand.I18N_PREFIX.Options.EmbedImageUrl.Text)

            val embedThumbnailUrl = optionalString("embed_thumbnail_url", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)

            val embedColor = optionalString("embed_color", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)

            val threadId = optionalString("thread_id", I18N_PREFIX.Options.ThreadId.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true) // Defer the message ephemerally because we don't want users looking at the webhook URL

            val webhookUrl = args[options.webhookUrl]
            val message = args[options.message]
            val username = args[options.username]
            val avatarUrl = args[options.avatarUrl]

            // Embed Stuff
            val embedTitle = args[options.embedTitle]
            val embedDescription = args[options.embedDescription]
            val embedImageUrl = args[options.embedImageUrl]
            val embedThumbnailUrl = args[options.embedThumbnailUrl]
            val embedColor = args[options.embedColor]

            val color = embedColor?.let {
                try {
                    Color.fromString(it).rgb
                } catch (e: IllegalArgumentException) {
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(WebhookCommand.I18N_PREFIX.InvalidEmbedColor),
                            Emotes.Error
                        )
                    }
                }
            }

            val threadId = args[options.threadId]?.toLongOrNull()

            sendMessageViaWebhook(context, webhookUrl, threadId) {
                buildJsonObject {
                    put("content", message)
                    put("username", username)
                    put("avatar_url", avatarUrl)

                    if (embedTitle != null || embedDescription != null || embedImageUrl != null || embedThumbnailUrl != null) {
                        putJsonArray("embeds") {
                            addJsonObject {
                                put("title", embedTitle)
                                put("description", embedDescription)

                                if (embedImageUrl != null)
                                    putJsonObject("image") {
                                        put("url", embedImageUrl)
                                    }

                                if (embedThumbnailUrl != null)
                                    putJsonObject("thumbnail") {
                                        put("url", embedThumbnailUrl)
                                    }

                                if (color != null)
                                    put("color", color)
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: Migrate this to JDA
    //class WebhookEditSimpleExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    //    inner class Options : LocalizedApplicationCommandOptions(loritta) {
    //        val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)
    //
    //        val messageId = string("message_id", WebhookCommand.I18N_PREFIX.Options.MessageId.Text)
    //
    //        val message = string("message", WebhookCommand.I18N_PREFIX.Options.Message.Text)
    //
    //        val embedTitle = optionalString("embed_title", WebhookCommand.I18N_PREFIX.Options.EmbedTitle.Text)
    //
    //        val embedDescription = optionalString("embed_description", WebhookCommand.I18N_PREFIX.Options.EmbedDescription.Text)
    //
    //        val embedImageUrl = optionalString("embed_image_url", WebhookCommand.I18N_PREFIX.Options.EmbedImageUrl.Text)
    //
    //        val embedThumbnailUrl = optionalString("embed_thumbnail_url", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)
    //
    //        val embedColor = optionalString("embed_color", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)
    //    }
    //
    //    override val options = Options()
    //
    //    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
    //        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL
    //
    //        val webhookUrl = args[options.webhookUrl]
    //        val messageId = WebhookCommandUtils.getRawMessageIdOrFromURLOrFail(context, args[options.messageId])
    //        val message = args[options.message]
    //
    //        // Embed Stuff
    //        val embedTitle = args[options.embedTitle]
    //        val embedDescription = args[options.embedDescription]
    //        val embedImageUrl = args[options.embedImageUrl]
    //        val embedThumbnailUrl = args[options.embedThumbnailUrl]
    //        val embedColor = args[options.embedColor]
    //
    //        WebhookCommandUtils.editMessageViaWebhook(context, webhookUrl, messageId) {
    //            val embed = if (embedTitle != null || embedDescription != null || embedImageUrl != null || embedThumbnailUrl != null) {
    //                EmbedRequest(
    //                    title = embedTitle?.optional() ?: Optional(),
    //                    description = embedDescription?.optional() ?: Optional(),
    //                    image = embedImageUrl?.let { EmbedImageRequest(it) }?.optional() ?: Optional(),
    //                    thumbnail = embedThumbnailUrl?.let { EmbedThumbnailRequest(it) }?.optional() ?: Optional(),
    //                    color = embedColor?.let {
    //                        try {
    //                            Color.fromString(it)
    //                        } catch (e: IllegalArgumentException) {
    //                            context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.InvalidEmbedColor))
    //                        }
    //                    }?.toKordColor()?.optional() ?: Optional()
    //                )
    //            } else null
    //
    //            WebhookEditMessageRequest(
    //                message
    //                    .replace(
    //                        "\\n",
    //                        "\n"
    //                    ) // TODO: When Discord supports multi line options, then we don't need this anymore :D
    //                    .optional(),
    //                embeds = if (embed != null) listOf(embed).optional() else Optional()
    //            )
    //        }
    //    }
    //}

    inner class WebhookSendRepostExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val webhookUrl = string("webhook_url", I18N_PREFIX.Options.WebhookUrl.Text)

            val messageUrl = string("message_url", I18N_PREFIX.Options.MessageUrl.Text)

            val username = optionalString("username", I18N_PREFIX.Options.Username.Text)

            val avatarUrl = optionalString("avatar_url", I18N_PREFIX.Options.AvatarUrl.Text)

            val embedTitle = optionalString("embed_title", I18N_PREFIX.Options.EmbedTitle.Text)

            val embedDescription = optionalString("embed_description", I18N_PREFIX.Options.EmbedDescription.Text)

            val embedImageUrl = optionalString("embed_image_url", I18N_PREFIX.Options.EmbedImageUrl.Text)

            val embedThumbnailUrl = optionalString("embed_thumbnail_url", I18N_PREFIX.Options.EmbedThumbnailUrl.Text)

            val embedColor = optionalString("embed_color", I18N_PREFIX.Options.EmbedThumbnailUrl.Text)

            val threadId = optionalString("thread_id", I18N_PREFIX.Options.ThreadId.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true) // Defer the message ephemerally because we don't want users looking at the webhook URL

            val webhookUrl = args[options.webhookUrl]
            val messageUrl = args[options.messageUrl]
            val username = args[options.username]
            val avatarUrl = args[options.avatarUrl]

            // Embed Stuff
            val embedTitle = args[options.embedTitle]
            val embedDescription = args[options.embedDescription]
            val embedImageUrl = args[options.embedImageUrl]
            val embedThumbnailUrl = args[options.embedThumbnailUrl]
            val embedColor = args[options.embedColor]

            val color = embedColor?.let {
                try {
                    Color.fromString(it).rgb
                } catch (e: IllegalArgumentException) {
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(WebhookCommand.I18N_PREFIX.InvalidEmbedColor),
                            Emotes.Error
                        )
                    }
                }
            }

            val threadId = args[options.threadId]?.toLongOrNull()

            val matcher = messageUrlRegex.find(messageUrl) ?: context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.InvalidMessageUrl
                    ),
                    Emotes.Error
                )
            }

            val channel = loritta.lorittaShards.shardManager.getChannel<MessageChannel>(matcher.groupValues[2].toLong())
            val retrievedMessage = channel?.retrieveMessageById(matcher.groupValues[3])
                ?.await()
                ?: context.fail(true) {
                    styled(
                        context.i18nContext.get(WebhookCommand.I18N_PREFIX.MessageToBeRepostedDoesNotExist),
                        Emotes.Error
                    )
                }

            sendMessageViaWebhook(context, webhookUrl, threadId) {
                buildJsonObject {
                    put("content", retrievedMessage.contentRaw)
                    put("username", username)
                    put("avatar_url", avatarUrl)

                    if (embedTitle != null || embedDescription != null || embedImageUrl != null || embedThumbnailUrl != null) {
                        putJsonArray("embeds") {
                            addJsonObject {
                                put("title", embedTitle)
                                put("description", embedDescription)

                                if (embedImageUrl != null)
                                    putJsonObject("image") {
                                        put("url", embedImageUrl)
                                    }

                                if (embedThumbnailUrl != null)
                                    putJsonObject("thumbnail") {
                                        put("url", embedThumbnailUrl)
                                    }

                                if (color != null)
                                    put("color", color)
                            }
                        }
                    }
                }
            }
        }
    }

    //class WebhookEditRepostExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    //    inner class Options : LocalizedApplicationCommandOptions(loritta) {
    //        val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)
    //
    //        val messageId = string("message_id", WebhookCommand.I18N_PREFIX.Options.MessageId.Text)
    //
    //        val messageUrl = string("message_url", WebhookCommand.I18N_PREFIX.Options.MessageUrl.Text)
    //
    //        val embedTitle = optionalString("embed_title", WebhookCommand.I18N_PREFIX.Options.EmbedTitle.Text)
    //
    //        val embedDescription = optionalString("embed_description", WebhookCommand.I18N_PREFIX.Options.EmbedDescription.Text)
    //
    //        val embedImageUrl = optionalString("embed_image_url", WebhookCommand.I18N_PREFIX.Options.EmbedImageUrl.Text)
    //
    //        val embedThumbnailUrl = optionalString("embed_thumbnail_url", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)
    //
    //        val embedColor = optionalString("embed_color", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)
    //    }
    //
    //    override val options = Options()
    //
    //    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
    //        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL
    //
    //        val webhookUrl = args[options.webhookUrl]
    //        val messageId = WebhookCommandUtils.getRawMessageIdOrFromURLOrFail(context, args[options.messageId])
    //        val message = args[options.messageUrl]
    //
    //        val matcher = WebhookCommandUtils.messageUrlRegex.find(message) ?: context.failEphemerally(
    //            context.i18nContext.get(
    //                WebhookCommand.I18N_PREFIX.InvalidMessageUrl
    //            )
    //        )
    //        val retrievedMessage = WebhookCommandUtils.getMessageOrFail(context, rest.channel,
    //            Snowflake(matcher.groupValues[2].toLong()),
    //            Snowflake(matcher.groupValues[3].toLong())
    //        )
    //
    //        // Embed Stuff
    //        val embedTitle = args[options.embedTitle]
    //        val embedDescription = args[options.embedDescription]
    //        val embedImageUrl = args[options.embedImageUrl]
    //        val embedThumbnailUrl = args[options.embedThumbnailUrl]
    //        val embedColor = args[options.embedColor]
    //
    //        WebhookCommandUtils.editMessageViaWebhook(context, webhookUrl, messageId) {
    //            val embed = if (embedTitle != null || embedDescription != null || embedImageUrl != null || embedThumbnailUrl != null) {
    //                EmbedRequest(
    //                    title = embedTitle?.optional() ?: Optional(),
    //                    description = embedDescription?.optional() ?: Optional(),
    //                    image = embedImageUrl?.let { EmbedImageRequest(it) }?.optional() ?: Optional(),
    //                    thumbnail = embedThumbnailUrl?.let { EmbedThumbnailRequest(it) }?.optional() ?: Optional(),
    //                    color = embedColor?.let {
    //                        try {
    //                            Color.fromString(it)
    //                        } catch (e: IllegalArgumentException) {
    //                            context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.InvalidEmbedColor))
    //                        }
    //                    }?.toKordColor()?.optional() ?: Optional()
    //                )
    //            } else null
    //
    //            WebhookEditMessageRequest(
    //                WebhookCommandUtils.cleanUpRetrievedMessageContent(retrievedMessage).optional(),
    //                embeds = if (embed != null) listOf(embed).optional() else Optional()
    //            )
    //        }
    //    }
    //}

    inner class WebhookSendJsonExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val webhookUrl = string("webhook_url", I18N_PREFIX.Options.WebhookUrl.Text)

            val json = string("json", I18N_PREFIX.Options.Json.Text)

            val threadId = optionalString("thread_id", I18N_PREFIX.Options.ThreadId.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true) // Defer the message ephemerally because we don't want users looking at the webhook URL

            val webhookUrl = args[options.webhookUrl]
            val message = args[options.json]
            val threadId = args[options.threadId]?.toLongOrNull()

            sendMessageViaWebhook(context, webhookUrl, threadId) {
                decodeRequestFromString(context, message)
            }
        }
    }

    inner class WebhookEditJsonExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val webhookUrl = string("webhook_url", I18N_PREFIX.Options.WebhookUrl.Text)

            val messageId = string("message_id", I18N_PREFIX.Options.MessageId.Text)

            val json = string("json", I18N_PREFIX.Options.Json.Text)

            val threadId = optionalString("thread_id", I18N_PREFIX.Options.ThreadId.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true) // Defer the message ephemerally because we don't want users looking at the webhook URL

            val webhookUrl = args[options.webhookUrl]
            val messageId = getRawMessageIdOrFromURLOrFail(context, args[options.messageId])
            val message = args[options.json]
            val threadId = args[options.threadId]?.toLongOrNull()

            editMessageViaWebhook(context, webhookUrl, messageId, threadId) {
                decodeRequestFromString(context, message)
            }
        }
    }

    suspend fun sendMessageViaWebhook(
        context: UnleashedContext,
        webhookUrl: String,
        threadId: Long?,
        requestBuilder: () -> (JsonObject)
    ) {
        val matchResult = WebhookUtils.webhookRegex.find(webhookUrl) ?: context.fail(true) {
            styled(
                context.i18nContext.get(I18N_PREFIX.InvalidWebhookUrl),
                Emotes.Error
            )
        }

        val webhookId = matchResult.groupValues[1].toLong()
        val webhookToken = matchResult.groupValues[2]

        // First we need to get the webhook's info
        val webhook = context.jda.retrieveWebhookById(webhookId).await()
        val targetChannel = webhook.channel
        if (targetChannel is ForumChannel && threadId == null) {
            context.fail(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.TargetChannelIsAForumButNoThreadIdWasProvided),
                    Emotes.Error
                )
            }
        }

        val webhookClient = WebhookClient.createClient(context.jda, webhookId.toString(), webhookToken)

        // Can we workaround this by using horrible hacks?
        // Yes we can!
        val request = requestBuilder.invoke()

        val content = getAsStringOrNull(request, "content")
        val avatarUrl = getAsStringOrNull(request, "avatar_url")

        // Validations before sending the message
        // Avatar URL
        validateURLAsHttpOrHttps(
            context,
            avatarUrl,
            I18N_PREFIX.InvalidAvatarUrl
        )

        val sentMessage = webhookClient.sendMessage(
            try {
                MessageUtils.generateMessage(
                    request.toString(),
                    null,
                    emptyMap(),
                    true
                )
            } catch (e: IllegalStateException) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidMessage),
                        Emotes.Error
                    )
                }
                return
            }
        )
            .setUsername(getAsStringOrNull(request, "username"))
            .setAvatarUrl(avatarUrl)
            .setThreadId(threadId ?: 0L)
            .await()

        // I think these are always present, but who knows
        // The guild ID is always NOT present
        val messageId = sentMessage.idLong

        sendWebhookSuccessMessage(
            context,
            messageId,
            I18N_PREFIX.Send.Success
        )
    }

    suspend fun editMessageViaWebhook(
        context: UnleashedContext,
        webhookUrl: String,
        messageId: Long,
        threadId: Long?,
        requestBuilder: () -> (JsonObject)
    ) {
        val matchResult = WebhookUtils.webhookRegex.find(webhookUrl) ?: context.fail(true) {
            styled(
                context.i18nContext.get(I18N_PREFIX.InvalidWebhookUrl),
                Emotes.Error
            )
        }

        val webhookId = matchResult.groupValues[1].toLong()
        val webhookToken = matchResult.groupValues[2]

        // First we need to get the webhook's info
        val webhook = context.jda.retrieveWebhookById(webhookId).await()
        val targetChannel = webhook.channel
        if (targetChannel is ForumChannel && threadId == null) {
            context.fail(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.TargetChannelIsAForumButNoThreadIdWasProvided),
                    Emotes.Error
                )
            }
        }

        val webhookClient = WebhookClient.createClient(context.jda, webhookId.toString(), webhookToken)

        // Can we workaround this by using horrible hacks?
        // Yes we can!
        val request = requestBuilder.invoke()
        val avatarUrl = getAsStringOrNull(request, "avatar_url")

        // Validations before sending the message
        // Avatar URL
        validateURLAsHttpOrHttps(
            context,
            avatarUrl,
            I18N_PREFIX.InvalidAvatarUrl
        )

        val sentMessage = webhookClient.editMessageById(
            messageId,
            MessageEditData.fromCreateData(
                MessageUtils.generateMessage(
                    request.toString(),
                    null,
                    emptyMap(),
                    true
                )
            )
        ).await()

        // I think these are always present, but who knows
        // The guild ID is always NOT present
        val messageId = sentMessage.idLong

        sendWebhookSuccessMessage(
            context,
            messageId,
            I18N_PREFIX.Edit.Success
        )
    }

    fun decodeRequestFromString(context: UnleashedContext, input: String): JsonObject {
        try {
            val parsedToJsonElement = Json.parseToJsonElement(input).jsonObject

            val rebuiltJson = buildJsonObject {
                for (entry in parsedToJsonElement.entries) {
                    if (entry.key == "embed") {
                        putJsonArray("embeds") {
                            add(entry.value)
                        }
                    } else {
                        put(entry.key, entry.value)
                    }
                }
            }

            return rebuiltJson
        } catch (e: Exception) {
            // Invalid message
            if (!input.startsWith("{")) // Doesn't seem to be a valid JSON, maybe they would prefer using "/webhook send simple"?
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidJsonMessageNoCurlyBraces(context.loritta.commandMentions.webhookSendSimple, context.loritta.commandMentions.webhookSendRepost)),
                        Emotes.Error
                    )
                }
            else
                context.fail(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidJsonMessage),
                        Emotes.Error
                    )
                }
        }
    }

    private fun validateMessageLength(context: UnleashedContext, optionalContent: String?) {
        optionalContent?.length?.let {
            if (it > DiscordResourceLimits.Message.Length)
                context.fail(true) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.MessageTooBig(
                                DiscordResourceLimits.Message.Length
                            )
                        ),
                        Emotes.Error
                    )
                }
        }
    }

    /**
     * Validates if the [url] is a valid HTTP or HTTPS URL via [URLUtils.isValidHttpOrHttpsURL], if it isn't, the command will fail ephemerally with the [message]
     */
    private fun validateURLAsHttpOrHttps(context: UnleashedContext, url: String?, message: StringI18nData) {
        if (url != null && !URLUtils.isValidHttpOrHttpsURL(url))
            context.fail(true) {
                styled(
                    context.i18nContext.get(message),
                    Emotes.Error
                )
            }
    }

    private fun validateEmbedAndAppend(context: UnleashedContext, embed: JsonObject, builder: AbstractMessageBuilder<*, *>) {
        // Embeds
        val embedBuilder = EmbedBuilder()
        var totalEmbedLength = 0

        val title = getAsStringOrNull(embed, "title")
        val titleLength = title?.length
        if (titleLength != null && titleLength > DiscordResourceLimits.Embed.Title)
            context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.EmbedTitleTooBig(
                            DiscordResourceLimits.Embed.Title
                        )
                    ),
                    Emotes.Error
                )
            }

        totalEmbedLength += titleLength ?: 0
        if (title != null)
            embedBuilder.setTitle(title, getAsStringOrNull(embed, "url"))

        val description = getAsStringOrNull(embed, "description")
        val descriptionLength = description?.length

        if (descriptionLength != null && descriptionLength > DiscordResourceLimits.Embed.Description)
            context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.EmbedDescriptionTooBig(
                            DiscordResourceLimits.Embed.Description
                        )
                    ),
                    Emotes.Error
                )
            }

        totalEmbedLength += descriptionLength ?: 0
        if (description != null)
            embedBuilder.setDescription(description)

        val fields = embed["fields"]?.jsonArray
            ?.filterIsInstance<JsonObject>()
        val fieldsSize = fields?.size
        if (fieldsSize != null && fieldsSize > DiscordResourceLimits.Embed.FieldsPerEmbed)
            context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.TooManyEmbedFields(
                            DiscordResourceLimits.Embed.FieldsPerEmbed
                        )
                    ),
                    Emotes.Error
                )
            }

        if (fields != null) {
            for (field in fields) {
                val fieldName = getAsStringOrNull(field, "name") ?: " "
                val fieldValue = getAsStringOrNull(field, "value") ?: " "
                val fieldNameLength = fieldName.length
                val fieldValueLength = fieldValue.length

                if (fieldNameLength > DiscordResourceLimits.Embed.Field.Name)
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.EmbedFieldNameTooBig(
                                    DiscordResourceLimits.Embed.Field.Name
                                )
                            ),
                            Emotes.Error
                        )
                    }

                if (fieldValueLength > DiscordResourceLimits.Embed.Field.Value)
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.EmbedFieldValueTooBig(
                                    DiscordResourceLimits.Embed.Field.Value
                                )
                            ),
                            Emotes.Error
                        )
                    }

                totalEmbedLength += fieldNameLength
                totalEmbedLength += fieldValueLength

                embedBuilder.addField(
                    fieldName,
                    fieldValue,
                    getAsBooleanOrNull(field, "inline") ?: true
                )
            }
        }

        val footer = embed["footer"]
        if (footer is JsonObject) {
            val footerText = getAsStringOrNull(footer, "text")
            if (footerText != null) {
                val footerIconUrl = getAsStringOrNull(footer, "icon_url")
                val footerTextLength = footerText.length

                if (footerTextLength > DiscordResourceLimits.Embed.Footer.Text)
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                WebhookCommand.I18N_PREFIX.EmbedFooterTooBig(
                                    DiscordResourceLimits.Embed.Footer.Text
                                )
                            ),
                            Emotes.Error
                        )
                    }

                totalEmbedLength += footerTextLength

                embedBuilder.setFooter(footerText, footerIconUrl)
            }
        }

        val author = embed["author"]
        if (author is JsonObject) {
            val authorName = getAsStringOrNull(author, "name")
            if (authorName != null) {
                val authorLength = authorName.length

                if (authorLength > DiscordResourceLimits.Embed.Author.Name)
                    context.fail(true) {
                        styled(
                            context.i18nContext.get(
                                WebhookCommand.I18N_PREFIX.EmbedAuthorNameTooBig(
                                    DiscordResourceLimits.Embed.Author.Name
                                )
                            ),
                            Emotes.Error
                        )
                    }

                totalEmbedLength += authorLength
                embedBuilder.setAuthor(authorName, getAsStringOrNull(author, "icon_url"), getAsStringOrNull(author, "url"))
            }
        }

        // Images
        val image = embed["image"]
        if (image is JsonObject) {
            val url = getAsStringOrNull(image, "url")
            validateURLAsHttpOrHttps(
                context,
                url,
                I18N_PREFIX.InvalidEmbedImageUrl
            )

            embedBuilder.setImage(url)
        }

        val thumbnail = embed["thumbnail"]
        if (thumbnail is JsonObject) {
            val url = getAsStringOrNull(thumbnail, "url")
            validateURLAsHttpOrHttps(
                context,
                url,
                I18N_PREFIX.InvalidEmbedThumbnailUrl
            )

            embedBuilder.setThumbnail(url)
        }

        val color = embed["color"]?.jsonPrimitive?.intOrNull
        if (color != null)
            embedBuilder.setColor(color)

        if (totalEmbedLength > DiscordResourceLimits.Embed.TotalCharacters)
            context.fail(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.EmbedTooBig(
                            DiscordResourceLimits.Embed.TotalCharacters
                        )
                    ),
                    Emotes.Error
                )
            }

        builder.setEmbeds(embedBuilder.build())
    }

    private suspend fun sendWebhookSuccessMessage(context: UnleashedContext, messageId: Long?, message: StringI18nData) = context.reply(true) {
        styled(
            "**${context.i18nContext.get(message)}**",
            Emotes.LoriYay
        )

        if (messageId != null) {
            styled(
                context.i18nContext.get(WebhookCommand.I18N_PREFIX.MessageId(messageId.toString()))
            )
        }

        styled(
            "*${context.i18nContext.get(WebhookCommand.I18N_PREFIX.WatchOutDontSendTheWebhookUrlToOtherUsers)}*"
        )
    }

    /**
     * Gets a Message ID from the [input].
     *
     * * The input is parsed as a Long, if it fails...
     * * The input is parsed as Message URL and the Message ID is extracted from the message, if it fails...
     * * The command fails ephemerally
     */
    private fun getRawMessageIdOrFromURLOrFail(context: UnleashedContext, input: String): Long {
        return input.toLongOrNull() ?: messageUrlRegex.find(input)?.groupValues?.get(3)?.toLongOrNull() ?: context.fail(true) {
            styled(
                context.i18nContext.get(I18N_PREFIX.InvalidMessageUrl),
                Emotes.Error
            )
        }
    }

    private fun getAsJsonPrimitiveOrNull(json: JsonObject, field: String) = json[field]?.jsonPrimitive
    private fun getAsStringOrNull(json: JsonObject, field: String) = json[field]?.jsonPrimitive?.contentOrNull
    private fun getAsBooleanOrNull(json: JsonObject, field: String) = json[field]?.jsonPrimitive?.booleanOrNull
}