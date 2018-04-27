package com.mrpowergamerbr.loritta.utils.gabriela

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonIgnore
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

class GabrielaMessage @BsonCreator constructor(
		@BsonProperty("_id")
		@get:[BsonIgnore]
		val messageId: ObjectId,
		@BsonProperty("localeId")
		val localeId: String
) {
	var questionWords = mutableSetOf<String>()
	var answers = mutableListOf<GabrielaAnswer>()
}