package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object MarrySonhosTransactionsLog : LongIdTable() {
    val user = reference("user", Profiles).index()
    val marriage = reference("marriage", Marriages)
    val sonhos = long("sonhos")
    val timestamp = timestamp("timestamp")
}