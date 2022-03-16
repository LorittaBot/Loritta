package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object PaymentSonhosTransactionResults : LongIdTable() {
    val givenBy = reference("given_by", Profiles)
    val receivedBy = reference("received_by", Profiles)
    val sonhos = long("sonhos")
    val timestamp = timestampWithTimeZone("timestamp").index()
}