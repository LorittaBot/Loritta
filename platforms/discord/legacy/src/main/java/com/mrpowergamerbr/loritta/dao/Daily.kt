package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.Dailies
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Daily(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Daily>(Dailies)

	var receivedById by Dailies.receivedById
	var receivedAt by Dailies.receivedAt
	var ip by Dailies.ip
	var email by Dailies.email
	var userAgent by Dailies.userAgent
}