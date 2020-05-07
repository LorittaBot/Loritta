package net.perfectdreams.loritta.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object TrackedRssFeeds : LongIdTable() {
    val guildId = long("guild").index()
    val channelId = long("channel")
    val feedUrl = text("feed_url")
    val message = text("message")
}