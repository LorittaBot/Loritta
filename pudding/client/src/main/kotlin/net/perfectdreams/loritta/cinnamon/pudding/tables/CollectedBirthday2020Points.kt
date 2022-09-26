package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object CollectedBirthday2020Points : LongIdTable() {
	val user = reference("user", Profiles).index()
	val message = reference("message", Birthday2020Drops).index()
	val points = integer("points")
}