package com.mrpowergamerbr.loritta.utils.eventlog

import org.mongodb.morphia.annotations.Entity
import org.mongodb.morphia.annotations.Id
import org.mongodb.morphia.annotations.Indexed
import java.util.*

@Entity(value = "storedmessages")
class StoredMessage {
	@Indexed
	@Id
	lateinit var messageId: String
	@Indexed(expireAfterSeconds = 1209600)
	val dateCreated = Date()
	lateinit var authorName: String
	lateinit var content: String
	lateinit var authorId: String
	lateinit var channelId: String
	var attachments: MutableList<String> = mutableListOf<String>()

	constructor() {

	}

	constructor(messageId: String, authorName: String, content: String, authorId: String, channelId: String, attachments: MutableList<String>) {
		this.messageId = messageId
		this.authorName = authorName
		this.content = content
		this.authorId = authorId
		this.channelId = channelId
		this.attachments = attachments
	}
}