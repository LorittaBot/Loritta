package net.perfectdreams.loritta.common.entities

interface Identifiable {
	val id: Long
	val idAsString: String
		get() = id.toString()
}