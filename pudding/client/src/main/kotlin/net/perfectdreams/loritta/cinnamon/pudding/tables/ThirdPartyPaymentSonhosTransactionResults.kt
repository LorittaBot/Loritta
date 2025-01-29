package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ThirdPartyPaymentSonhosTransactionResults : LongIdTable() {
    val tokenUser = long("token_user")
    val givenBy = reference("given_by", Profiles)
    val receivedBy = reference("received_by", Profiles)
    val sonhos = long("sonhos")
    val tax = long("tax")
    val taxPercentage = double("tax_percentage")
    val timestamp = timestampWithTimeZone("timestamp").index()
    val reason = text("reason")
}