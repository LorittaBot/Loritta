package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.sql.ReferenceOption

object Profiles : SnowflakeTable() {
	val xp = long("xp").index()
	val lastMessageSentAt = long("last_message_sent_at")
	val lastMessageSentHash = integer("last_message_sent_hash")
	val lastCommandSentAt = long("last_command_sent_at").nullable()
	val money = long("money").index()
	var isAfk = bool("isAfk")
	var afkReason = text("afkReason").nullable()
	var settings = reference("settings", UserSettings, onDelete = ReferenceOption.CASCADE).index()
	var marriage = reference("marriage", Marriages).nullable().index()
}