package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object LorittaPartnersServerInvites : LongIdTable() {
    val userId = long("user_id").index()
    val guildId = long("guild_id").index()
    val inviteCode = text("invite_code")
    val createdAt = timestampWithTimeZone("created_at").index()
    val expiresAt = timestampWithTimeZone("expires_at")
}
