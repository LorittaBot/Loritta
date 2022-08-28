package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object SchemaVersion : UUIDTable() {
    val version = integer("version")
}