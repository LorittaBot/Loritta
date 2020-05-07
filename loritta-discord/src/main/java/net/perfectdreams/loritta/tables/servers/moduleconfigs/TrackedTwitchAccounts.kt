package net.perfectdreams.loritta.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object TrackedTwitchAccounts : LongIdTable() {
    val guildId = long("guild").index()
    val channelId = long("channel")
    val twitchUserId = long("twitch_user_id").index()
    val message = text("message")
    val webhookUrl = text("webhook_url").nullable()
}