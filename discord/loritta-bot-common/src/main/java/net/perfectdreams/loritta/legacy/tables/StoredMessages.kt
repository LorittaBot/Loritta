package net.perfectdreams.loritta.legacy.tables

import net.perfectdreams.loritta.legacy.utils.exposed.array
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.TextColumnType

object StoredMessages : LongIdTable() {
	val authorId = long("author_id").index()
	val channelId = long("channel_id")
	val content = text("content")
	val createdAt = long("created_at").index()
	val editedAt = long("edited_at").nullable()
	val storedAttachments = array<String>("stored_attachments", TextColumnType())
	val initializationVector = text("initialization_vector")
}