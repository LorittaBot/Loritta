package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.rest.service.RestClient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

class WebhookEditJsonExecutor(val rest: RestClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(WebhookEditJsonExecutor::class) {
        object Options : CommandOptions() {
            val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)
                .register()

            val messageId = string("message_id", WebhookCommand.I18N_PREFIX.Options.MessageId.Text)
                .register()

            val json = string("json", WebhookCommand.I18N_PREFIX.Options.Json.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL

        val webhookUrl = args[Options.webhookUrl]
        val messageId = WebhookCommandUtils.getRawMessageIdOrFromURLOrFail(context, args[Options.messageId])
        val message = args[Options.json]

        WebhookCommandUtils.editMessageViaWebhook(context, webhookUrl, messageId) {
            Json.decodeFromString(message)
        }
    }
}