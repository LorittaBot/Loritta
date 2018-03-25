package com.mrpowergamerbr.loritta.utils.gabriela

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

object Gabriela {
	class GabrielaMessage @BsonCreator constructor(
			@BsonProperty("_id")
			_question: String, // Guild ID
			@BsonProperty("localeId")
			val localeId: String
	) {
		@BsonProperty("_id")
		val question = _question
		var answers = mutableListOf<String>()
	}
}