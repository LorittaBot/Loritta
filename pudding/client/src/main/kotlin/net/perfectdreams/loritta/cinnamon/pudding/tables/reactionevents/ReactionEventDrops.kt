package net.perfectdreams.loritta.cinnamon.pudding.tables.reactionevents

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ReactionEventDrops : LongIdTable() {
    val event = text("event").index()
    val reactionSetId = uuid("emoji_id").index()
    val guildId = long("guild")
    val channelId = long("channel")
    val messageId = long("message")
    val createdAt = timestampWithTimeZone("created_at")
}