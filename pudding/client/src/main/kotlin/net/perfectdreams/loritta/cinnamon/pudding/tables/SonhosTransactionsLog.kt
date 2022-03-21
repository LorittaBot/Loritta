package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object SonhosTransactionsLog : LongIdTable() {
    val timestamp = timestampWithTimeZone("timestamp")
    val user = reference("user", Profiles).index()
}