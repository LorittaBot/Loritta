package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Birthday2020Drops : LongIdTable() {
	val guildId = long("guild")
	val channelId = long("channel")
	val messageId = long("message")
	val createdAt = long("created_at")
}