package com.mrpowergamerbr.loritta.utils

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

fun main(args: Array<String>) {
	val pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
			fromProviders(PojoCodecProvider.builder().automatic(true).build()))

	val mongoClient = MongoClient("localhost", MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build())

	val db = mongoClient.getDatabase("pojo_test")

	val dbCodec = db.withCodecRegistry(pojoCodecRegistry);

	val serverConfig = ServerConfig("-12")
	// serverConfig.guildUserData.add(LorittaGuildUserData().apply { "-1" })
	val collection = dbCodec.getCollection("test", ServerConfig::class.java)

	collection.insertOne(serverConfig)
	for (test in collection.find()) {
		for (a in test.guildUserData) {
			// println(a.userId)
		}
	}
}

class HelloWorld @BsonCreator constructor(@BsonProperty("_id") test: String) {
	@BsonProperty("_id")
	val realId = test
	var subClass = SubClass()
}

class SubClass {
	var howdy: String = "???"
}