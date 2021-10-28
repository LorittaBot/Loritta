package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions

class WebhookSendJsonExecutor(val rest: RestClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(WebhookSendJsonExecutor::class) {
        object Options : CommandOptions() {
            val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)
                .register()

            val json = string("json", WebhookCommand.I18N_PREFIX.Options.Json.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL

        val webhookUrl = args[Options.webhookUrl]
        val message = args[Options.json]

        WebhookCommandUtils.sendMessageViaWebhook(context, webhookUrl) {
            WebhookCommandUtils.decodeRequestFromString(context, message)
        }
    }
}