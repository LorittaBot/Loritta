package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object StoredMessages : LongIdTable() {
	val authorId = long("author_id").index()
	val channelId = long("channel_id")
	val createdAt = timestampWithTimeZone("created_at").index()
	val savedMessageDataVersion = integer("message_data_version")
	val encryptedSavedMessageData = text("encrypted_saved_message_data")
	val initializationVector = text("initialization_vector")
}