package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object BoostedCandyChannels : LongIdTable() {
	val user = reference("user", Profiles)
	val guildId = long("guild")
	val channelId = long("channel")
	val givenAt = long("given_at")
	val expiresAt = long("expires_at")
}