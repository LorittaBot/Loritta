package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Reputations : LongIdTable() {
	val givenById = long("given_by").index()
	val givenByIp = text("given_by_ip").index()
	val givenByEmail = text("given_by_email").index()
	val receivedById = long("received_by").index()
	val receivedAt = long("received_at")
	val content = text("content").nullable()
}