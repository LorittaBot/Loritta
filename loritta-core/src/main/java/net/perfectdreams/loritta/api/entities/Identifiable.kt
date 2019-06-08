package net.perfectdreams.loritta.api.entities

interface Identifiable {
	val id: Long
	val idAsString: String
		get() = id.toString()
}