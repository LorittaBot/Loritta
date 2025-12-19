package net.perfectdreams.dora.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object UserWebsiteSessions : LongIdTable() {
    val token = text("token").uniqueIndex()
    val userId = long("user").index()
    val createdAt = timestampWithTimeZone("created_at")
    val refreshedAt = timestampWithTimeZone("refreshed_at")
    val lastUsedAt = timestampWithTimeZone("last_used_at").index()
    val cookieSetAt = timestampWithTimeZone("cookie_set_at")
    val cookieMaxAge = integer("cookie_max_age")
    val tokenType = text("token_type")
    val accessToken = text("access_token")
    val expiresIn = long("expires_in")
    val refreshToken = text("refresh_token")
    val scope = array<String>("scope")
}