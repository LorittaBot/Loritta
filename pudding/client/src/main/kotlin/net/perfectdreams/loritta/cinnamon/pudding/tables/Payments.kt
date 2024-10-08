package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.jsonb
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentGateway
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
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
    val metadata = jsonb("metadata").nullable()
    val coupon = optReference("coupon", WebsiteDiscountCoupons)
}