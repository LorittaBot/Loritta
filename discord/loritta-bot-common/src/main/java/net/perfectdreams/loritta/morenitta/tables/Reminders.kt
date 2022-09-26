package net.perfectdreams.loritta.morenitta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Reminders : LongIdTable() {
	val userId = long("user_id").index()
	val channelId = long("channel_id")
	val remindAt = long("remind_at")
	val content = text("content")
}