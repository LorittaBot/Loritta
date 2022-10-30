package net.perfectdreams.loritta.deviouscache.server.tables

import org.jetbrains.exposed.sql.Table

object GuildMembers : Table() {
    val guildId = long("guild_id").index()
    val userId = long("user_id").index()
    val data = text("data")

    override val primaryKey = PrimaryKey(guildId, userId)
}