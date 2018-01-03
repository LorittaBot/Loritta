package com.mrpowergamerbr.loritta.utils

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.codecs.pojo.annotations.BsonId


fun main(args: Array<String>) {
	val pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
			fromProviders(PojoCodecProvider.builder().automatic(true).build()))

	val mongoClient = MongoClient("localhost", MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build())

	val db = mongoClient.getDatabase("pojo_test")

	val dbCodec = db.withCodecRegistry(pojoCodecRegistry);

	val collection = dbCodec.getCollection("hello_world", HelloWorld::class.java)

	val hello = HelloWorld("test", "thaay")

	collection.insertOne(hello)
}

class HelloWorld(@BsonId val id: String, val test: String, val subClass: SubClass) {

}

class SubClass(val howdy: String) {

}