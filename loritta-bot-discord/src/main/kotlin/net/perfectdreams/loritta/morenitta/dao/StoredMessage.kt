package net.perfectdreams.loritta.morenitta.dao

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.StoredMessages
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.eventlog.EventLog
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class StoredMessage(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<StoredMessage>(StoredMessages)

	var authorId by StoredMessages.authorId
	var channelId by StoredMessages.channelId
	var createdAt by StoredMessages.createdAt
	var savedMessageDataVersion by StoredMessages.savedMessageDataVersion
	var encryptedSavedMessageData by StoredMessages.encryptedSavedMessageData
	var initializationVector by StoredMessages.initializationVector

	fun encryptAndSetContent(loritta: LorittaBot, value: SavedMessage) {
		val encrypted = EventLog.encryptMessage(loritta, Json.encodeToString(value))
		this.savedMessageDataVersion = 1
		this.initializationVector = encrypted.initializationVector
		this.encryptedSavedMessageData = encrypted.encryptedMessage
	}

	fun decryptContent(loritta: LorittaBot) = Json.decodeFromString<SavedMessage>(EventLog.decryptMessage(loritta, initializationVector, encryptedSavedMessageData))
}