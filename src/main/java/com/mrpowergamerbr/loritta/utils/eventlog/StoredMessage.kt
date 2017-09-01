package com.mrpowergamerbr.loritta.utils.eventlog

import org.mongodb.morphia.annotations.*

@Entity(value = "storedmessages")
@Indexes(Index(options = IndexOptions(unique = true, expireAfterSeconds = 1209600)))
class StoredMessage {
	@Indexed
	@Id
	lateinit var messageId: String
	lateinit var authorName: String
	lateinit var content: String
	lateinit var authorId: String
	lateinit var channelId: String
	lateinit var attachments: MutableList<String>
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