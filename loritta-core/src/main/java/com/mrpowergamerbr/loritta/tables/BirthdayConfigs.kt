package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object BirthdayConfigs : LongIdTable() {
    val enabled = bool("custom_badge").default(false)
    val channelId = long("channel").nullable()
}