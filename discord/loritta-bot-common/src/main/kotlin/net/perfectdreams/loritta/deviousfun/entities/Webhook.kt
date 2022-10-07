package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordWebhook
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.WebhookType
import net.perfectdreams.loritta.deviousfun.JDA

class Webhook(
    val jda: JDA,
    val channel: Channel,
    val ownerAsUser: User?,
    val webhook: DiscordWebhook
) : IdentifiableSnowflake {
    override val idSnowflake: Snowflake
        get() = webhook.id
    val token: String?
        get() = webhook.token.value
    val type: WebhookType
        get() = webhook.type
    val url: String
        get() = if (token != null) "https://discord.com/api/webhooks/$idSnowflake/$token" else error("Can't get the URL of a webhook that doesn't have a token! (User created webhook)")
}