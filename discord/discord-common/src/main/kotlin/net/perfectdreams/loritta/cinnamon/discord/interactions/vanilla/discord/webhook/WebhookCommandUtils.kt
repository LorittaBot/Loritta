package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.webhook

import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.rest.json.request.EmbedRequest
import dev.kord.rest.json.request.WebhookEditMessageRequest
import dev.kord.rest.json.request.WebhookExecuteRequest
import dev.kord.rest.request.RestRequestException
import dev.kord.rest.service.ChannelService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.URLUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.WebhookUtils

object WebhookCommandUtils {
    suspend fun sendMessageViaWebhook(
        context: ApplicationCommandContext,
        webhookUrl: String,
        requestBuilder: () -> (WebhookExecuteRequest)
    ) {
        val matchResult = WebhookUtils.webhookRegex.find(webhookUrl) ?: context.failEphemerally(
            context.i18nContext.get(
                WebhookCommand.I18N_PREFIX.InvalidWebhookUrl
            )
        )

        val webhookId = Snowflake(matchResult.groupValues[1].toULong())
        val webhookToken = matchResult.groupValues[2]

        // Can we workaround this by using horrible hacks?
        // Yes we can!
        val request = requestBuilder.invoke()

        // Validations before sending the message
        // Message Length
        validateMessageLength(context, request.content)
        // Avatar URL
        validateURLAsHttpOrHttps(context, request.avatar.value, WebhookCommand.I18N_PREFIX.InvalidAvatarUrl)
        // Embeds
        validateEmbeds(context, request.embeds)

        val sentMessage = try {
            WebhookUtils.executeWebhook(
                webhookId,
                webhookToken,
                true,
                request = request
            )
        } catch (e: RestRequestException) {
            if (e.status.code == 401) {
                // The webhook doesn't exist!
                context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.WebhookDoesNotExist))
            }

            // I don't know what happened so let's just leave!
            throw e
        }

        // I think these are always present, but who knows
        // The guild ID is always NOT present
        val messageId = sentMessage?.id?.value

        sendWebhookSuccessMessage(context, messageId?.toLong(), WebhookCommand.I18N_PREFIX.Send.Success)
    }

    suspend fun editMessageViaWebhook(
        context: ApplicationCommandContext,
        webhookUrl: String,
        messageId: Long,
        requestBuilder: () -> (WebhookEditMessageRequest)
    ) {
        val matchResult = WebhookUtils.webhookRegex.find(webhookUrl) ?: context.failEphemerally(context.i18nContext.get(
            WebhookCommand.I18N_PREFIX.InvalidWebhookUrl))

        val webhookId = Snowflake(matchResult.groupValues[1].toULong())
        val webhookToken = matchResult.groupValues[2]

        // Can we workaround this by using horrible hacks?
        // Yes we can!
        val request = requestBuilder.invoke()

        // Validations before sending the message
        // Message Length
        validateMessageLength(context, request.content)
        // Embeds
        validateEmbeds(context, request.embeds)

        val sentMessage = try {
            WebhookUtils.editWebhookMessage(
                webhookId,
                webhookToken,
                Snowflake(messageId),
                request = request
            )
        } catch (e: RestRequestException) {
            if (e.status.code == 401) {
                // The webhook doesn't exist!
                context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.WebhookDoesNotExist))
            } else if (e.status.code == 404) {
                // The message doesn't exist or wasn't sent via this webhook!
                context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.MessageDoesNotExistOrWasntSentByThisWebhook))
            }

            // I don't know what happened so let's just leave!
            throw e
        }

        // The guild ID is always NOT present so don't bother trying to get it
        sendWebhookSuccessMessage(context, messageId, WebhookCommand.I18N_PREFIX.Edit.Success)
    }

    val messageUrlRegex = Regex("/channels/([0-9]+)/([0-9]+)/([0-9]+)")

    /**
     * Cleans up the retrieved message, removing Loritta's "Message sent by" tag
     */
    fun cleanUpRetrievedMessageContent(input: DiscordMessage): String {
        var content = input.content
        if (input.author.id == Snowflake(297153970613387264)) {
            // Message sent by Loritta via "+say", clean up the message by automatically drop all lines starting with "Message sent by"
            content.lines()
                .filterNot {
                    it.startsWith("<:lori_coffee:727631176432484473> *Mensagem enviada por ") || it.startsWith("<:lori_coffee:727631176432484473> *Message sent by ")
                }
                .joinToString("\n")
                .also { content = it }
        }

        return content
    }

    /**
     * Gets a Message ID from the [input].
     *
     * * The input is parsed as a Long, if it fails...
     * * The input is parsed as Message URL and the Message ID is extracted from the message, if it fails...
     * * The command fails ephemerally
     */
    fun getRawMessageIdOrFromURLOrFail(context: ApplicationCommandContext, input: String): Long {
        return input.toLongOrNull() ?: messageUrlRegex.find(input)?.groupValues?.get(3)?.toLongOrNull() ?: context.failEphemerally("a")
    }

    /**
     * Gets a message or, if it doesn't exists, fails ephemerally
     */
    suspend fun getMessageOrFail(context: ApplicationCommandContext, service: ChannelService, channelId: Snowflake, messageId: Snowflake) = try {
        service.getMessage(channelId, messageId)
    } catch (e: RestRequestException) {
        if (e.status.code == 404)
            context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.MessageToBeRepostedDoesNotExist))

        throw e
    }

    inline fun <reified T> decodeRequestFromString(context: ApplicationCommandContext, input: String): T {
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

            return Json.decodeFromJsonElement(rebuiltJson)
        } catch (e: Exception) {
            // Invalid message
            if (!input.startsWith("{")) // Doesn't seem to be a valid JSON, maybe they would prefer using "/webhook send simple"?
                context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.InvalidJsonMessageNoCurlyBraces))
            else
                context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.InvalidJsonMessage))
        }
    }

    private fun validateMessageLength(context: ApplicationCommandContext, optionalContent: Optional<String?>) {
        optionalContent.value?.length?.let {
            if (it > DiscordResourceLimits.Message.Length)
                context.failEphemerally(context.i18nContext.get(
                    WebhookCommand.I18N_PREFIX.MessageTooBig(
                        DiscordResourceLimits.Message.Length
                    )))
        }
    }

    private fun validateEmbeds(context: ApplicationCommandContext, optionalEmbeds: Optional<List<EmbedRequest>>) {
        // Embeds
        val embeds = optionalEmbeds.value
        embeds?.size?.let {
            if (it > DiscordResourceLimits.Message.EmbedsPerMessage)
                context.failEphemerally(
                    context.i18nContext.get(
                        WebhookCommand.I18N_PREFIX.TooManyEmbeds(
                            DiscordResourceLimits.Message.EmbedsPerMessage
                        )
                    )
                )
        }

        if (embeds != null) {
            var totalEmbedLength = 0

            for (embed in embeds) {
                val titleLength = embed.title.value?.length
                if (titleLength != null && titleLength > DiscordResourceLimits.Embed.Title)
                    context.failEphemerally(
                        context.i18nContext.get(
                            WebhookCommand.I18N_PREFIX.EmbedTitleTooBig(
                                DiscordResourceLimits.Embed.Title
                            )
                        )
                    )

                totalEmbedLength += titleLength ?: 0

                val descriptionLength = embed.description.value?.length
                if (descriptionLength != null && descriptionLength > DiscordResourceLimits.Embed.Description)
                    context.failEphemerally(
                        context.i18nContext.get(
                            WebhookCommand.I18N_PREFIX.EmbedDescriptionTooBig(
                                DiscordResourceLimits.Embed.Description
                            )
                        )
                    )

                totalEmbedLength += descriptionLength ?: 0

                val fields = embed.fields.value
                val fieldsSize = fields?.size
                if (fieldsSize != null && fieldsSize > DiscordResourceLimits.Embed.FieldsPerEmbed)
                    context.failEphemerally(
                        context.i18nContext.get(
                            WebhookCommand.I18N_PREFIX.TooManyEmbedFields(
                                DiscordResourceLimits.Embed.FieldsPerEmbed
                            )
                        )
                    )

                if (fields != null) {
                    for (field in fields) {
                        val fieldName = field.name
                        val fieldValue = field.value
                        val fieldNameLength = fieldName.length
                        val fieldValueLength = fieldValue.length

                        if (fieldNameLength > DiscordResourceLimits.Embed.Field.Name)
                            context.failEphemerally(
                                context.i18nContext.get(
                                    WebhookCommand.I18N_PREFIX.EmbedFieldNameTooBig(
                                        DiscordResourceLimits.Embed.Field.Name
                                    )
                                )
                            )
                        if (fieldValueLength > DiscordResourceLimits.Embed.Field.Value)
                            context.failEphemerally(
                                context.i18nContext.get(
                                    WebhookCommand.I18N_PREFIX.EmbedFieldValueTooBig(
                                        DiscordResourceLimits.Embed.Field.Value
                                    )
                                )
                            )

                        totalEmbedLength += fieldNameLength
                        totalEmbedLength += fieldValueLength
                    }
                }

                val footerLength = embed.footer.value?.text?.length
                if (footerLength != null && footerLength > DiscordResourceLimits.Embed.Footer.Text)
                    context.failEphemerally(
                        context.i18nContext.get(
                            WebhookCommand.I18N_PREFIX.EmbedFooterTooBig(
                                DiscordResourceLimits.Embed.Footer.Text
                            )
                        )
                    )

                totalEmbedLength += footerLength ?: 0

                val authorLength = embed.author.value?.name?.value?.length
                if (authorLength != null && authorLength > DiscordResourceLimits.Embed.Author.Name)
                    context.failEphemerally(
                        context.i18nContext.get(
                            WebhookCommand.I18N_PREFIX.EmbedAuthorNameTooBig(
                                DiscordResourceLimits.Embed.Author.Name
                            )
                        )
                    )

                // Images
                validateURLAsHttpOrHttps(context, embed.image.value?.url, WebhookCommand.I18N_PREFIX.InvalidEmbedImageUrl)
                validateURLAsHttpOrHttps(context, embed.thumbnail.value?.url, WebhookCommand.I18N_PREFIX.InvalidEmbedThumbnailUrl)

                totalEmbedLength += authorLength ?: 0
            }

            if (totalEmbedLength > DiscordResourceLimits.Embed.TotalCharacters)
                context.failEphemerally(
                    context.i18nContext.get(
                        WebhookCommand.I18N_PREFIX.EmbedTooBig(
                            DiscordResourceLimits.Embed.TotalCharacters
                        )
                    )
                )
        }
    }

    /**
     * Validates if the [url] is a valid HTTP or HTTPS URL via [URLUtils.isValidHttpOrHttpsURL], if it isn't, the command will fail ephemerally with the [message]
     */
    private fun validateURLAsHttpOrHttps(context: ApplicationCommandContext, url: String?, message: StringI18nData) {
        if (url != null && !URLUtils.isValidHttpOrHttpsURL(url))
            context.failEphemerally(context.i18nContext.get(message))
    }

    private suspend fun sendWebhookSuccessMessage(context: ApplicationCommandContext, messageId: Long?, message: StringI18nData) = context.sendEphemeralMessage {
        styled(
            "**${context.i18nContext.get(message)}**",
            net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriYay
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
}