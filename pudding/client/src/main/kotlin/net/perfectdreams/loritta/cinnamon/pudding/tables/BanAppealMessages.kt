package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object BanAppealMessages : LongIdTable() {
    val guildId = long("guild_id").index()
    val channelId = long("channel_id").index()
    val messageId = long("message_id").index()
    val sentAt = timestampWithTimeZone("sent_at").index()
    val appeal = reference("appeal", BanAppeals).index()
}