package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column

object Reputations : LongIdTable() {
	override val id: Column<EntityID<Long>>
		get() = long("id").primaryKey().autoIncrement().entityId()
	val givenById = long("given_by").index()
	val givenByIp = text("given_by_ip").index()
	val givenByEmail = text("given_by_email").index()
	val receivedById = long("received_by").index()
	val receivedAt = long("received_at")
	val content = text("content")
}