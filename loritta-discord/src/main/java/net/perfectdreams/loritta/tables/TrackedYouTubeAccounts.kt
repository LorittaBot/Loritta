package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object TrackedYouTubeAccounts : LongIdTable() {
    val guildId = long("guild").index()
    val channelId = long("channel")
    val youTubeChannelId = text("youtube_channel_id").index()
    val message = text("message")
    val webhookUrl = text("webhook_url").nullable()
}