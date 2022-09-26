package net.perfectdreams.loritta.legacy.api.entities

interface Guild : Identifiable {
	val name: String
	val members: List<Member>
	val icon: String?
	val messageChannels: List<MessageChannel>
}