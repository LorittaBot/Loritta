package com.mrpowergamerbr.loritta.tables

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Timer
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.exposed.Json
import com.mrpowergamerbr.loritta.utils.exposed.array
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.EnumerationColumnType
import java.time.DayOfWeek

object Timers : LongIdTable() {
	val guildId = long("guild").index()
	val channelId = long("channel").index()
	val startsAt = long("starts_at")
	val repeatCount = integer("repeat_count").nullable()
	val repeatDelay = long("repeat_delay")
	val activeOnDays = array<DayOfWeek>("active_on_days", EnumerationColumnType(DayOfWeek::class.java))
	val effects = array<Timer.TimerEffect>("effects", Json(ServerConfig::class.java, Loritta.GSON))
}