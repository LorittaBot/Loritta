package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty

class TextChannelConfig @BsonCreator constructor(
		@BsonProperty("id")
		@get:[BsonIgnore]
		val id: String?
) {
	// Unused
	@AllowReflection
	var isBlacklisted = false
	@AllowReflection
	var automodConfig = AutomodConfig()
	@AllowReflection
	var memberCounterConfig: MemberCounterConfig? = null
}