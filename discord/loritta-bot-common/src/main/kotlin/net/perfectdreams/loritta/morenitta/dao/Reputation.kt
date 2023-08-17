package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
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