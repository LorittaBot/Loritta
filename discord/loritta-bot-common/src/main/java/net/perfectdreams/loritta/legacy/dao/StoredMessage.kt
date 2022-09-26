package net.perfectdreams.loritta.legacy.dao

import net.perfectdreams.loritta.legacy.tables.StoredMessages
import net.perfectdreams.loritta.legacy.utils.eventlog.EventLog
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class StoredMessage(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<StoredMessage>(StoredMessages)

	var authorId by StoredMessages.authorId
	var channelId by StoredMessages.channelId
	var encryptedContent by StoredMessages.content
	var createdAt by StoredMessages.createdAt
	var editedAt by StoredMessages.editedAt
	var storedAttachments by StoredMessages.storedAttachments
	var initializationVector by StoredMessages.initializationVector

	var content: String
		get() {
			// decrypt
			return EventLog.decryptMessage(initializationVector, encryptedContent)
		}
		set(value) {
			// encrypt
			val encrypted = EventLog.encryptMessage(value)
			this.initializationVector = encrypted.initializationVector
			this.encryptedContent = encrypted.encryptedMessage
		}
}