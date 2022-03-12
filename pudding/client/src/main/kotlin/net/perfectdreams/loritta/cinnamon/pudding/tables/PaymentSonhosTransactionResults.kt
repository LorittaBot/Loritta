package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object PaymentSonhosTransactionResults : LongIdTable() {
    val givenBy = reference("given_by", Profiles)
    val receivedBy = reference("received_by", Profiles)
    val sonhos = long("sonhos")
    val timestamp = timestamp("timestamp").index()
}