package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.BirthdayTeam
import org.jetbrains.exposed.dao.id.LongIdTable

object Birthday2020Players : LongIdTable() {
	val user = reference("user", Profiles).index()
	val team = enumeration("team", BirthdayTeam::class).index()
}