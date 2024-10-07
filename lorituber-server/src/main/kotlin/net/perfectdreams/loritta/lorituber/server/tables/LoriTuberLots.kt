package net.perfectdreams.loritta.lorituber.server.tables

import org.jetbrains.exposed.sql.Table

object LoriTuberLots : Table() {
    // I have places to be
    val id = uuid("id").uniqueIndex()
    val data = blob("data")
}