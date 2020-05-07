package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object WhitelistedTransactionIds : LongIdTable() {
	val userId = long("user").index()
	val whitelistedAt = long("whitelisted_at")
	val reason = text("reason").nullable()
}