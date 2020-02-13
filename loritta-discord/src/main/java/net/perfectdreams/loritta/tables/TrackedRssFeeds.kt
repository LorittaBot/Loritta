package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object TrackedRssFeeds : LongIdTable() {
    val guildId = long("guild").index()
    val channelId = long("channel")
    val feedUrl = text("feed_url")
    val message = text("message")
}