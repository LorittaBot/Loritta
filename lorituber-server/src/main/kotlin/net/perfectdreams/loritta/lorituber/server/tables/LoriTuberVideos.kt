package net.perfectdreams.loritta.lorituber.server.tables

import org.jetbrains.exposed.sql.Table

object LoriTuberVideos : Table() {
    val id = uuid("id").uniqueIndex()
    val data = blob("data")
}