package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object GuildProfiles : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user").index()
	val xp = long("xp").index()
	val quickPunishment = bool("quick_punishment")
	val money = decimal("money", 19, 14).index()
}