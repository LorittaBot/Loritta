package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.StoredMessages
import net.perfectdreams.loritta.morenitta.utils.eventlog.EventLog
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

    fun encryptAndSetContent(loritta: LorittaBot, value: String) {
        val encrypted = EventLog.encryptMessage(loritta, value)
        this.initializationVector = encrypted.initializationVector
        this.encryptedContent = encrypted.encryptedMessage
    }

    fun decryptContent(loritta: LorittaBot) = EventLog.decryptMessage(loritta, initializationVector, encryptedContent)
}