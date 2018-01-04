package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class TextChannelConfig @BsonCreator constructor(@BsonProperty("id") _id: String?) {
	val id = _id

	// Unused
	var isBlacklisted = false
	var automodConfig = AutomodConfig()
}