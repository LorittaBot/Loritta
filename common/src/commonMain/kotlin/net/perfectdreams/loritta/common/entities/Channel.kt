package net.perfectdreams.loritta.common.entities

interface Channel {
	val name: String?
	val participants: List<Member>
}