package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object ReactionOptions : LongIdTable() {
    val guildId = long("guild").index()
    val textChannelId = long("channel").index()
    val messageId = long("message_id").index()
    val reaction = text("reaction").index()
    val locks = array<String>("locks", TextColumnType())
    val roleIds = array<String>("roles", TextColumnType())
}