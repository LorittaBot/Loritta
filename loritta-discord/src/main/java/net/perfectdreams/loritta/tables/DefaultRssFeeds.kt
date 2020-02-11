package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object DefaultRssFeeds : LongIdTable() {
    val feedId = text("feed_id").index()
    val feedUrl = text("feed_url")
}