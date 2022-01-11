package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object SonhosTransactionsLog : LongIdTable() {
    val timestamp = timestamp("timestamp")
    val user = reference("user", Profiles).index()
}