package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object BackgroundPayments : LongIdTable() {
	val userId = long("user").index()
	val background = reference("background", Backgrounds).index()
	val boughtAt = long("bought_at")
	val cost = long("money")
}