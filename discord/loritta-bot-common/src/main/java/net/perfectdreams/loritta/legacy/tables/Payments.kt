package net.perfectdreams.loritta.legacy.tables

import net.perfectdreams.loritta.legacy.utils.exposed.rawJsonb
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.utils.payments.PaymentGateway
import net.perfectdreams.loritta.legacy.utils.payments.PaymentReason
import org.jetbrains.exposed.dao.id.LongIdTable

object Payments : LongIdTable() {
    val userId = long("user").index()
    val gateway = enumeration("gateway", PaymentGateway::class)
    val reason = enumeration("reason", PaymentReason::class)
    val money = decimal("money", 12, 2).index()
    val createdAt = long("created_at")
    val paidAt = long("paid_at").nullable()
    val expiresAt = long("expires_at").nullable()
    val discount = double("discount").nullable()
    val referenceId = uuid("reference_id").nullable()
    val metadata = rawJsonb("metadata", gson).nullable()
}