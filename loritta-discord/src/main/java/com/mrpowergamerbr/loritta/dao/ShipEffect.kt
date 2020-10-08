package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.ShipEffects
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ShipEffect(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<ShipEffect>(ShipEffects)

	var buyerId by ShipEffects.buyerId
	var user1Id by ShipEffects.user1Id
	var user2Id by ShipEffects.user2Id
	var editedShipValue by ShipEffects.editedShipValue
	var expiresAt by ShipEffects.expiresAt
}