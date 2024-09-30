package net.perfectdreams.loritta.lorituber.server.tables

import org.jetbrains.exposed.sql.Table

object LoriTuberCharactersAlt : Table() {
    val id = long("id").uniqueIndex()
    val data = blob("data")
}