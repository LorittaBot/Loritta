package com.mrpowergamerbr.loritta.utils.eventlog

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class StoredMessage @BsonCreator constructor(@BsonProperty("_id") _messageId: String) {
	@BsonProperty("_id")
	val messageId = _messageId
	val dateCreated = System.currentTimeMillis()
	lateinit var authorName: String
	lateinit var content: String
	lateinit var authorId: String
	lateinit var channelId: String
	var attachments: MutableList<String> = mutableListOf<String>()

	constructor(messageId: String, authorName: String, content: String, authorId: String, channelId: String, attachments: MutableList<String>) : this(messageId) {
		this.authorName = authorName
		this.content = content
		this.authorId = authorId
		this.channelId = channelId
		this.attachments = attachments
	}
}