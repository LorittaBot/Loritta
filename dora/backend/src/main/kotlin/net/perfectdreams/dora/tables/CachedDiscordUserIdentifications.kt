package net.perfectdreams.dora.tables

import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object CachedDiscordUserIdentifications : UniqueSnowflakeTable() {
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    val username = text("username")
    val discriminator = text("discriminator")
    val globalName = text("global_name").nullable()
    val avatarId = text("avatar_id").nullable()
    val mfaEnabled = bool("mfa_enabled")
    val locale = text("locale")
    val banner = text("banner").nullable()
    val accentColor = integer("accent_color").nullable()
    val verified = bool("verified")
    val email = text("email").nullable()
    val flags = integer("flags")
    val premiumType = integer("premium_type")
    val publicFlags = integer("public_flags")
}