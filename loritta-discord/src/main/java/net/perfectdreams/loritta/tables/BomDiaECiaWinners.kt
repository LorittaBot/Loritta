package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object BomDiaECiaWinners : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user").index()
	val wonAt = long("won_at")
	val prize = decimal("prize", 12, 2)
}