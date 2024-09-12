package net.perfectdreams.loritta.morenitta.utils.devious

import org.jetbrains.exposed.dao.id.UUIDTable

object SessionCacheMetadata : UUIDTable() {
    val content = text("content")
}