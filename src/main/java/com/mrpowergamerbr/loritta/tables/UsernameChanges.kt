package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column

object UsernameChanges : LongIdTable() {
	override val id: Column<EntityID<Long>>
		get() = long("id").primaryKey().autoIncrement().entityId()

	val userId = long("user_id").index()
	val username = text("username")
	val discriminator = text("discriminator")
	val changedAt = long("changed_at")
}