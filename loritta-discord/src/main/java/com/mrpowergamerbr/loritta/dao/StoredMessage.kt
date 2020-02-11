package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.StoredMessages
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class StoredMessage(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<StoredMessage>(StoredMessages)

	var authorId by StoredMessages.authorId
	var channelId by StoredMessages.channelId
	var content by StoredMessages.content
	var createdAt by StoredMessages.createdAt
	var editedAt by StoredMessages.editedAt
	var storedAttachments by StoredMessages.storedAttachments
}