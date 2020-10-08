package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.tables.GuildProfiles
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildProfile(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<GuildProfile>(GuildProfiles)

	var guildId by GuildProfiles.guildId
	var userId by GuildProfiles.userId
	var xp by GuildProfiles.xp
	var quickPunishment by GuildProfiles.quickPunishment
	var money by GuildProfiles.money
	var isInGuild by GuildProfiles.isInGuild

	fun getCurrentLevel(): XpWrapper {
		return XpWrapper((xp / 1000).toInt(), xp)
	}

	data class XpWrapper constructor(val currentLevel: Int, val expLeft: Long)
}