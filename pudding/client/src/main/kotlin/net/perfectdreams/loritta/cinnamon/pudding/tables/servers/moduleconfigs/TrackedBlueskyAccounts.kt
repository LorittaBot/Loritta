package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object TrackedBlueskyAccounts : LongIdTable() {
    val guildId = long("guild").index()
    val channelId = long("channel")
    val repo = text("repo")
    val message = text("message")
    val addedAt = timestampWithTimeZone("added_at").nullable()
    val editedAt = timestampWithTimeZone("edited_at").nullable()
}