package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.Reminders
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Reminder(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Reminder>(Reminders)

	var userId by Reminders.userId
	var channelId by Reminders.channelId
	var remindAt by Reminders.remindAt
	var content by Reminders.content
}