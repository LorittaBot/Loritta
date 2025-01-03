package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.common.utils.TokenType
import org.jetbrains.exposed.dao.id.LongIdTable

object UserLorittaAPITokens : LongIdTable() {
    val tokenCreatorId = long("token_creator_id").index()
    val tokenUserId = long("token_user_id")
    val token = text("token").uniqueIndex()
    val tokenType = enumerationByName<TokenType>("token_type", 64).index().nullable()
    val generatedAt = timestampWithTimeZone("generated_at")
}