package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.webhook

import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import dev.kord.rest.json.request.EmbedImageRequest
import dev.kord.rest.json.request.EmbedRequest
import dev.kord.rest.json.request.EmbedThumbnailRequest
import dev.kord.rest.json.request.WebhookEditMessageRequest
import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.utils.Color
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.utils.toKordColor

class WebhookEditSimpleExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)

        val messageId = string("message_id", WebhookCommand.I18N_PREFIX.Options.MessageId.Text)

        val message = string("message", WebhookCommand.I18N_PREFIX.Options.Message.Text)

        val embedTitle = optionalString("embed_title", WebhookCommand.I18N_PREFIX.Options.EmbedTitle.Text)

        val embedDescription = optionalString("embed_description", WebhookCommand.I18N_PREFIX.Options.EmbedDescription.Text)

        val embedImageUrl = optionalString("embed_image_url", WebhookCommand.I18N_PREFIX.Options.EmbedImageUrl.Text)

        val embedThumbnailUrl = optionalString("embed_thumbnail_url", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)

        val embedColor = optionalString("embed_color", WebhookCommand.I18N_PREFIX.Options.EmbedThumbnailUrl.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL

        val webhookUrl = args[options.webhookUrl]
        val messageId = WebhookCommandUtils.getRawMessageIdOrFromURLOrFail(context, args[options.messageId])
        val message = args[options.message]

        // Embed Stuff
        val embedTitle = args[options.embedTitle]
        val embedDescription = args[options.embedDescription]
        val embedImageUrl = args[options.embedImageUrl]
        val embedThumbnailUrl = args[options.embedThumbnailUrl]
        val embedColor = args[options.embedColor]

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