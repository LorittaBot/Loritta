package net.perfectdreams.loritta.legacy.api.entities

interface Channel {
	val name: String?
	val participants: List<Member>
}