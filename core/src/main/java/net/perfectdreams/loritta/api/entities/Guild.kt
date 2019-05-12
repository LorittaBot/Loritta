package net.perfectdreams.loritta.api.entities

interface Guild : Unique {
	val name: String
	val members: List<Member>
	val iconUrl: String?
	val messageChannels: List<MessageChannel>
}