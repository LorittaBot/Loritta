package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.common.utils.DailyTaxPendingDirectMessageState
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.postgresEnumeration
import org.jetbrains.exposed.dao.id.LongIdTable

object DailyTaxPendingDirectMessages : LongIdTable() {
    val userId = long("user").index()
    val state = postgresEnumeration<DailyTaxPendingDirectMessageState>("state")
    val data = jsonb("data")
}