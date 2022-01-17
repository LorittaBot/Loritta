package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object CoinFlipBetGlobalMatchmakingQueue : LongIdTable() {
    val user = reference("user", Profiles)
    val userInteractionToken = text("user_interaction_token")
    val quantity = long("quantity").index()
    val language = text("language")
    val timestamp = timestamp("timestamp")
    val expiresAt = timestamp("expires_at")
}