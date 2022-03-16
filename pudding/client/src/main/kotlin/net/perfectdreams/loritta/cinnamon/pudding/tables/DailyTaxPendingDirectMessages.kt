package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.common.utils.DailyTaxPendingDirectMessageState
import net.perfectdreams.exposedpowerutils.sql.jsonb
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import org.jetbrains.exposed.dao.id.LongIdTable

object DailyTaxPendingDirectMessages : LongIdTable() {
    val userId = long("user").index()
    val state = postgresEnumeration<DailyTaxPendingDirectMessageState>("state")
    val data = jsonb("data")
}