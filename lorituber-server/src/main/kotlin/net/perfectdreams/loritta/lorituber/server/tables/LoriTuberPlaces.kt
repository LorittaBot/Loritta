package net.perfectdreams.loritta.lorituber.server.tables

import org.jetbrains.exposed.sql.Table

object LoriTuberPlaces : Table() {
    // I have places to be
    val id = text("id").uniqueIndex()
    val data = blob("data")
}