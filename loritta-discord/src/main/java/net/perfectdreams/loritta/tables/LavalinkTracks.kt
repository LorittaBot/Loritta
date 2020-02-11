package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object LavalinkTracks : LongIdTable() {
	val identifier = text("identifier").index()
	val trackData = text("track_data")
	val retrievedAt = long("retrieved_at")
}