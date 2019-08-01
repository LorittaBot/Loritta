package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.array
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.LongColumnType

object BirthdayConfigs : LongIdTable() {
    val enabled = bool("enabled").default(false)
    val channelId = long("channel").nullable().index()
    val roles = array<Long>("roles", LongColumnType()).nullable()
}