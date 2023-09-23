package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object YouTubeEventSubEvents : LongIdTable() {
    val event = text("event")
}