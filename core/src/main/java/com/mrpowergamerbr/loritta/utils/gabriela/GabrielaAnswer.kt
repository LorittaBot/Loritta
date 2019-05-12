package com.mrpowergamerbr.loritta.utils.gabriela

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class GabrielaAnswer @BsonCreator constructor(
		@BsonProperty("answer")
		val answer: String,
		@BsonProperty("submittedBy")
		val submittedBy: String) {
	var upvotes = mutableSetOf<String>()
	var downvotes = mutableSetOf<String>()
}