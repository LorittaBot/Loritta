package net.perfectdreams.loritta.common.exposed.dao

import net.perfectdreams.loritta.common.exposed.tables.CachedDiscordWebhooks
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CachedDiscordWebhook(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CachedDiscordWebhook>(CachedDiscordWebhooks)

    var channelId by CachedDiscordWebhooks.id
    var webhookId by CachedDiscordWebhooks.webhookId
    var webhookToken by CachedDiscordWebhooks.webhookToken
    var state by CachedDiscordWebhooks.state
    var updatedAt by CachedDiscordWebhooks.updatedAt
    var lastSuccessfullyExecutedAt by CachedDiscordWebhooks.lastSuccessfullyExecutedAt

    override fun toString(): String {
        return "CachedDiscordWebhook(channelId=$channelId, webhookId=$webhookId, webhookToken=$webhookToken, state=$state, updatedAt=$updatedAt, lastSuccessfullyExecutedAt=$lastSuccessfullyExecutedAt)"
    }
}