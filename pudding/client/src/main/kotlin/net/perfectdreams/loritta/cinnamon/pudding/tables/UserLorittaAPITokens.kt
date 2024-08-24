package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object UserLorittaAPITokens : LongIdTable() {
    val tokenCreatorId = long("token_creator_id").index()
    val tokenUserId = long("token_user_id")
    val token = text("token").uniqueIndex()
    val generatedAt = timestampWithTimeZone("generated_at")
}