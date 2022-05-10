package net.perfectdreams.loritta.cinnamon.platform.commands.discord.webhook

import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import dev.kord.rest.json.request.EmbedImageRequest
import dev.kord.rest.json.request.EmbedRequest
import dev.kord.rest.json.request.EmbedThumbnailRequest
import dev.kord.rest.json.request.WebhookEditMessageRequest
import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.common.utils.Color
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor

class WebhookEditSimpleExecutor(val rest: RestClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)
                .register()

            val messageId = string("message_id", WebhookCommand.I18N_PREFIX.Options.MessageId.Text)
                .register()

            val message = string("message", WebhookCommand.I18N_PREFIX.Options.Message.Text)
                .register()

            val embedTitle = optionalString("embed_title", WebhookCommand.I18N_PREFIX.Options.EmbedTitle.Text)
                .register()

            val embedDescription = optionalString("embed_description", WebhookCommand.I18N_PREFIX.Options.EmbedDescription.Text)
                .register()

            val embedImageUrl = optionalString("embed_image_url", WebhookCommand.I18N_PREFIX.Options.EmbedImageUrl.Text)
                .register()

            val embedThumbnailUrl = optionalString("embed_thumbnail_url", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)
                .register()

            val embedColor = optionalString("embed_color", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL

        val webhookUrl = args[Options.webhookUrl]
        val messageId = WebhookCommandUtils.getRawMessageIdOrFromURLOrFail(context, args[Options.messageId])
        val message = args[Options.message]

        // Embed Stuff
        val embedTitle = args[Options.embedTitle]
        val embedDescription = args[Options.embedDescription]
        val embedImageUrl = args[Options.embedImageUrl]
        val embedThumbnailUrl = args[Options.embedThumbnailUrl]
        val embedColor = args[Options.embedColor]

        WebhookCommandUtils.editMessageViaWebhook(context, webhookUrl, messageId) {
            val embed = if (embedTitle != null || embedDescription != null || embedImageUrl != null || embedThumbnailUrl != null) {
                EmbedRequest(
                    title = embedTitle?.optional() ?: Optional(),
                    description = embedDescription?.optional() ?: Optional(),
                    image = embedImageUrl?.let { EmbedImageRequest(it) }?.optional() ?: Optional(),
                    thumbnail = embedThumbnailUrl?.let { EmbedThumbnailRequest(it) }?.optional() ?: Optional(),
                    color = embedColor?.let {
                        try {
                            Color.fromString(it)
                        } catch (e: IllegalArgumentException) {
                            context.failEphemerally(context.i18nContext.get(WebhookCommand.I18N_PREFIX.InvalidEmbedColor))
                        }
                    }?.toKordColor()?.optional() ?: Optional()
                )
            } else null

            WebhookEditMessageRequest(
                message
                    .replace(
                        "\\n",
                        "\n"
                    ) // TODO: When Discord supports multi line options, then we don't need this anymore :D
                    .optional(),
                embeds = if (embed != null) listOf(embed).optional() else Optional()
            )
        }
    }
}