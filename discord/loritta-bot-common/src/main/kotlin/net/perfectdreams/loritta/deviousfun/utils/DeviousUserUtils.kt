package net.perfectdreams.loritta.deviousfun.utils

import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.DiscordPartialMessage

object DeviousUserUtils {
    fun isSenderWebhookOrSpecial(message: DiscordMessage) = message.author.discriminator == "0000" || message.author.system.discordBoolean || message.webhookId.value != null
    // Message Updates for webhooks may not have a "webhookId"
    fun isSenderWebhookOrSpecial(message: DiscordPartialMessage) = message.author.value?.discriminator == "0000" || message.author.value?.system?.discordBoolean == true || message.webhookId.value != null
}