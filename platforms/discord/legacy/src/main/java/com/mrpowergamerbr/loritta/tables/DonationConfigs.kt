package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object DonationConfigs : LongIdTable() {
    val customBadge = bool("custom_badge").default(false)
    val dailyMultiplier = bool("daily_multiplier").default(false)
}