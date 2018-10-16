package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.UsernameChanges
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass

class UsernameChange(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<UsernameChange>(UsernameChanges)

	var userId by UsernameChanges.userId
	var username by UsernameChanges.username
	var discriminator by UsernameChanges.discriminator
	var changedAt by UsernameChanges.changedAt
}