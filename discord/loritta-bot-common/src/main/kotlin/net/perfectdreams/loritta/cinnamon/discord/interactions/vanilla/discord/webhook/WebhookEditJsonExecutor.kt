package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.webhook

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.WebhookCommand
import net.perfectdreams.loritta.morenitta.LorittaBot

class WebhookEditJsonExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val webhookUrl = string("webhook_url", WebhookCommand.I18N_PREFIX.Options.WebhookUrl.Text)

        val messageId = string("message_id", WebhookCommand.I18N_PREFIX.Options.MessageId.Text)

        val json = string("json", WebhookCommand.I18N_PREFIX.Options.Json.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally() // Defer the message ephemerally because we don't want users looking at the webhook URL

        val webhookUrl = args[options.webhookUrl]
        val messageId = WebhookCommandUtils.getRawMessageIdOrFromURLOrFail(context, args[options.messageId])
        val message = args[options.json]

        WebhookCommandUtils.editMessageViaWebhook(context, webhookUrl, messageId) {
            WebhookCommandUtils.decodeRequestFromString(context, message)
        }
    }
}