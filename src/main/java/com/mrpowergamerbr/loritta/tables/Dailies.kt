package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object Dailies : LongIdTable() {
	val receivedById = long("received_by").index()
	val receivedAt = long("received_at").index()
	val ip = text("ip").index()
	val email = text("email").index()
}