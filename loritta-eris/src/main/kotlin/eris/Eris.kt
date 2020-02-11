package eris

import kotlin.js.Promise

@JsModule("eris")
external class Eris(token: String) {
	fun on(type: String, callback: dynamic)

	fun connect()
}

external class Message {
	val content: String
	val author: User
	val channel: TextChannel
	val mentions: Array<User>
}

external class TextChannel {
	fun createMessage(content: String): Promise<Message>
	fun createMessage(content: String, fileOptions: FileOptions): Promise<Message>
}

external class User {
	val avatar: String
	val avatarURL: String
	val bot: Boolean
	val id: String
	val username: String
	val mention: String
	val discriminator: String
}