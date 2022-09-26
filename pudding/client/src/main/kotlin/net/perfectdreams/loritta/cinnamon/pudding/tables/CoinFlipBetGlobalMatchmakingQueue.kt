package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object CoinFlipBetGlobalMatchmakingQueue : LongIdTable() {
    val user = reference("user", Profiles).index()
    val userInteractionToken = text("user_interaction_token")
    val quantity = long("quantity").index()
    val language = text("language")
    val timestamp = timestampWithTimeZone("timestamp")
    val expiresAt = timestampWithTimeZone("expires_at").index()
}