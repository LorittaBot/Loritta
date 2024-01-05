package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Reminders : LongIdTable() {
	val userId = long("user_id").index()
	val guildId = long("guild_id").nullable().index() // This is new, that's why it is nullable
	val channelId = long("channel_id")
	val remindAt = long("remind_at").index()
	val content = text("content")
}