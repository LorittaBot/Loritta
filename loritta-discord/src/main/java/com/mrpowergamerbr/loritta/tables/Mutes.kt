package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Mutes : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user").index()
	val punishedById = long("punished_by")
	val content = text("content").nullable()
	val receivedAt = long("received_at")
	val isTemporary = bool("temporary")
	val expiresAt = long("expires_at").nullable()
}