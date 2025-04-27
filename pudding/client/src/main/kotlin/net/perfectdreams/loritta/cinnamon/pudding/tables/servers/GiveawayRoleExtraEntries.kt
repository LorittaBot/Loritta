package net.perfectdreams.loritta.cinnamon.pudding.tables.servers

import org.jetbrains.exposed.dao.id.LongIdTable

object GiveawayRoleExtraEntries : LongIdTable() {
	val giveawayId = reference("giveaway_id", Giveaways.id).index()
	val roleId = long("role_id").index()
	val weight = integer("weight")
}