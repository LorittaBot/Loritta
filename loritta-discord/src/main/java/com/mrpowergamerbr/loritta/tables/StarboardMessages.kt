package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object StarboardMessages : LongIdTable() {
	val guildId = long("guild").index()
	val embedId = long("embed").index()
	val messageId = long("message").index()
}