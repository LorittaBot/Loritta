package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object UsernameChanges : LongIdTable() {
	val userId = long("user_id").index()
	val username = text("username")
	val discriminator = text("discriminator")
	val changedAt = long("changed_at")
}