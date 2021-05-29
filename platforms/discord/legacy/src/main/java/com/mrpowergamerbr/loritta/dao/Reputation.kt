package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.Reputations
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Reputation(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Reputation>(Reputations)

	var givenById by Reputations.givenById
	var givenByEmail by Reputations.givenByEmail
	var givenByIp by Reputations.givenByIp
	var receivedById by Reputations.receivedById
	var content by Reputations.content
	var receivedAt by Reputations.receivedAt
}