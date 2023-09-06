package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Halloween2019Players : LongIdTable() {
	val user = long("user")
}