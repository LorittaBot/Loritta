package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object SonhosTransferRequests : LongIdTable() {
    val giver = long("giver")
    val giverAcceptedAt = timestampWithTimeZone("giver_accepted_at").nullable()
    val receiver = long("receiver")
    val receiverAcceptedAt = timestampWithTimeZone("receiver_accepted_at").nullable()
    val quantity = long("quantity")
    val requestedAt = timestampWithTimeZone("requested_at")
    val expiresAt = timestampWithTimeZone("expires_at")
    val transferredAt = timestampWithTimeZone("transferred_at").nullable()
    val metadata = jsonb("metadata").nullable()
}