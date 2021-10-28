package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import dev.kord.rest.json.request.EmbedImageRequest
import dev.kord.rest.json.request.EmbedRequest
import dev.kord.rest.json.request.EmbedThumbnailRequest
import dev.kord.rest.json.request.WebhookExecuteRequest
import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.common.utils.Color
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor

class WebhookSendSimpleExecutor(val rest: RestClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(WebhookSendSimpleExecutor::class) {
        object Options : CommandOptions() {
            val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)
                .register()

            val message = string("message", WebhookCommand.I18N_PREFIX.Options.Message.Text)
                .register()

            val username = optionalString("username", WebhookCommand.I18N_PREFIX.Options.Username.Text)
                .register()

            val avatarUrl = optionalString("avatar_url", WebhookCommand.I18N_PREFIX.Options.AvatarUrl.Text)
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

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL

        val webhookUrl = args[Options.webhookUrl]
        val message = args[Options.message]
        val username = args[Options.username]
        val avatarUrl = args[Options.avatarUrl]

        // Embed Stuff
        val embedTitle = args[Options.embedTitle]
        val embedDescription = args[Options.embedDescription]
        val embedImageUrl = args[Options.embedImageUrl]
        val embedThumbnailUrl = args[Options.embedThumbnailUrl]
        val embedColor = args[Options.embedColor]

        WebhookCommandUtils.sendMessageViaWebhook(context, webhookUrl) {
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

            WebhookExecuteRequest(
                content = message
                    .replace("\\n", "\n") // TODO: When Discord supports multi line options, then we don't need this anymore :D
                    .optional(),
                username = username?.optional() ?: Optional(),
                avatar = avatarUrl?.optional() ?: Optional(),
                embeds = if (embed != null) listOf(embed).optional() else Optional()
            )
        }
    }
}