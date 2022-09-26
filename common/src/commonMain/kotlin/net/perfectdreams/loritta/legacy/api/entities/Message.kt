package net.perfectdreams.loritta.legacy.api.entities

interface Message {
	val author: User
	// val member: Member?
	val content: String
	val mentionedUsers: List<User>
	val channel: MessageChannel

	suspend fun delete()
}