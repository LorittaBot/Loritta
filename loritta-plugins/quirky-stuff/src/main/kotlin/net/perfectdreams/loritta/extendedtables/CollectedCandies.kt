package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.tables.Profiles
import org.jetbrains.exposed.dao.LongIdTable

object CollectedCandies : LongIdTable() {
	val user = reference("user", Profiles)
	val guildId = long("guild")
	val channelId = long("channel")
	val messageId = long("message")
}