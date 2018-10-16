package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column

object Reminders : LongIdTable() {
	override val id: Column<EntityID<Long>>
		get() = long("id").primaryKey().autoIncrement().entityId()
	val userId = long("user_id").index()
	val channelId = long("channel_id")
	val remindAt = long("created_at")
	val content = text("content")
}