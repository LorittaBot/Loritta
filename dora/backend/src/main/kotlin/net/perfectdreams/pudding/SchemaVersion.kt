package net.perfectdreams.pudding

import org.jetbrains.exposed.dao.id.UUIDTable

object SchemaVersion : UUIDTable() {
    val version = integer("version")
}