package net.perfectdreams.loritta.lorituber.server.tables

import org.jetbrains.exposed.sql.Table

object LoriTuberWorldTicks : Table() {
    val type = text("type").uniqueIndex()
    val data = blob("data")
}