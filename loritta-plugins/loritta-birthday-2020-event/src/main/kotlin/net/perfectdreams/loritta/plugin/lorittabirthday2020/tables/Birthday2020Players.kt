package net.perfectdreams.loritta.plugin.lorittabirthday2020.tables

import com.mrpowergamerbr.loritta.tables.Profiles
import net.perfectdreams.loritta.plugin.lorittabirthday2020.utils.BirthdayTeam
import org.jetbrains.exposed.dao.LongIdTable

object Birthday2020Players : LongIdTable() {
	val user = reference("user", Profiles).index()
	val team = enumeration("team", BirthdayTeam::class).index()
}