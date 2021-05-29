package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object ProfileDesignsPayments : LongIdTable() {
	val userId = long("user").index()
	val profile = reference("profile", ProfileDesigns).index()
	val boughtAt = long("bought_at")
	val cost = long("money")
}