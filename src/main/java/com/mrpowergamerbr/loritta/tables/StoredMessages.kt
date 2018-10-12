package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.array
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object StoredMessages : LongIdTable() {
	val authorId = long("author_id")
	val channelId = long("channel_id")
	val content = text("content")
	val createdAt = long("created_at")
	val editedAt = long("edited_at").nullable()
	val storedAttachments = array<String>("stored_attachments", TextColumnType())
}