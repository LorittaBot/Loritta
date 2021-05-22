package net.perfectdreams.loritta.api.entities

interface Channel {
	val name: String?
	val participants: List<Member>
}