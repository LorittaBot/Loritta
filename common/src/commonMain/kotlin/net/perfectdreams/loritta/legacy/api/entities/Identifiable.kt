package net.perfectdreams.loritta.legacy.api.entities

interface Identifiable {
	val id: Long
	val idAsString: String
		get() = id.toString()
}