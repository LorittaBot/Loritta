package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.tables.Profiles
import org.jetbrains.exposed.dao.id.LongIdTable

object CollectedCandies : LongIdTable() {
	val user = reference("user", Profiles).index()
	val guildId = long("guild")
	val channelId = long("channel")
	val messageId = long("message")
}