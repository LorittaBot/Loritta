package com.mrpowergamerbr.loritta.utils.gabriela

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

class GabrielaMessage @BsonCreator constructor(
		@BsonProperty("_id")
		_id: ObjectId,
		@BsonProperty("localeId")
		val localeId: String
) {
	@BsonProperty("_id")
	val id = _id
	var questionWords = mutableSetOf<String>()
	var answers = mutableListOf<GabrielaAnswer>()
}