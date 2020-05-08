package net.perfectdreams.loritta.plugin.funky.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object LavalinkSearchResults : LongIdTable() {
	val identifier = text("search_query").index()
	val trackData = text("track_data")
	val retrievedAt = long("retrieved_at")
}