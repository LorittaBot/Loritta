package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object ParallaxMetaStorages : LongIdTable() {
	val guildId = long("guild").index()
	val storageName = varchar("storage_name", 24).index()
	val data = text("data")
}