package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Warns : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user").index()
	val punishedById = long("punished_by")
	val content = text("content").nullable()
	val receivedAt = long("received_at")
}