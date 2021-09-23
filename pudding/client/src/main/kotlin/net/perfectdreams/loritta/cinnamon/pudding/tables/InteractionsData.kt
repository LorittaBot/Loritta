package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object InteractionsData : LongIdTable() {
    val data = jsonb("data")
    val addedAt = timestamp("added_at")
    val expiresAt = timestamp("expires_at")
}