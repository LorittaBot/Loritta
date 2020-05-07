package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Marriages : LongIdTable() {
	val user1 = long("user1").index()
	val user2 = long("user2").index()
	val marriedSince = long("married_since")
}