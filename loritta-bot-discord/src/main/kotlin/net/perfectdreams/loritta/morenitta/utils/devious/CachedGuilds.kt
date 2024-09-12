package net.perfectdreams.loritta.morenitta.utils.devious

import org.jetbrains.exposed.dao.id.LongIdTable

object CachedGuilds : LongIdTable() {
    val hashCode = integer("hash_code").index()
    val event = text("json")
}