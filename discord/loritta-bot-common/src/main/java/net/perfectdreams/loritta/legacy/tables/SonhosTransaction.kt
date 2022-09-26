package net.perfectdreams.loritta.legacy.tables

import net.perfectdreams.loritta.legacy.utils.exposed.rawJsonb
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.utils.SonhosPaymentReason
import org.jetbrains.exposed.dao.id.LongIdTable

object SonhosTransaction : LongIdTable() {
	val reason = enumeration("source", SonhosPaymentReason::class)
	val givenBy = long("given_by").index().nullable()
	val receivedBy = long("received_by").index().nullable()
	val givenAt = long("given_at")
	val quantity = decimal("quantity", 12, 2)
	val metadata = rawJsonb("metadata", gson).nullable()
}