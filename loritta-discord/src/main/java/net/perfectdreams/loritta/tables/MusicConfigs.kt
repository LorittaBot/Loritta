package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.array
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.LongColumnType

object MusicConfigs : LongIdTable() {
    val enabled = bool("enabled").default(false)
    val channels = array<Long>("no_xp_channels", LongColumnType())
    val hasMaxSecondRestriction = bool("has_max_second_restriction").default(true)
    val maxSeconds = long("max_seconds").default(600)
}