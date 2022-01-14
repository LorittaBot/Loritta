package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object CoinflipGlobalMatchmakingQueue : LongIdTable() {
    val user = reference("user", Profiles)
    val userInteractionToken = text("user_interaction_token")
    val quantity = long("quantity").index()
    val timestamp = timestamp("timestamp")
}