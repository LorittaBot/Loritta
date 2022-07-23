package net.perfectdreams.loritta.cinnamon.platform.commands.discord.webhook

import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class WebhookSendJsonExecutor(val rest: RestClient) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)
                .register()

            val json = string("json", WebhookCommand.I18N_PREFIX.Options.Json.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL

        val webhookUrl = args[Options.webhookUrl]
        val message = args[Options.json]

        WebhookCommandUtils.sendMessageViaWebhook(context, webhookUrl) {
            WebhookCommandUtils.decodeRequestFromString(context, message)
        }
    }
}