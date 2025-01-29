package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ThirdPartySonhosTransferRequests : LongIdTable() {
    val tokenUser = long("token_user").index()
    val giver = long("giver")
    val giverAcceptedAt = timestampWithTimeZone("giver_accepted_at").nullable()
    val receiver = long("receiver")
    val receiverAcceptedAt = timestampWithTimeZone("receiver_accepted_at").nullable()
    val quantity = long("quantity")
    val requestedAt = timestampWithTimeZone("requested_at")
    val expiresAt = timestampWithTimeZone("expires_at")
    val transferredAt = timestampWithTimeZone("transferred_at").nullable()
    val reason = text("reason")

    // The coinflip bet things have nullable tax and tax percentage, but honestly... that doesn't make any sense?!
    // Just let the value be zero if the tax is not present
    val tax = long("tax")
    val taxPercentage = double("tax_percentage")
}