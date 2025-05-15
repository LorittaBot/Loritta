package net.perfectdreams.loritta.morenitta.dao

import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriagesOld
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MarriageOld(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<MarriageOld>(MarriagesOld)

	var user1 by MarriagesOld.user1
	var user2 by MarriagesOld.user2
	var marriedSince by MarriagesOld.marriedSince
}