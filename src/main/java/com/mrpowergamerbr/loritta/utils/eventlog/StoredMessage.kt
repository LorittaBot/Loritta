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

	constructor() {

	}

	constructor(messageId: String, authorName: String, content: String) {
		this.messageId = messageId
		this.authorName = authorName
		this.content = content
	}
}