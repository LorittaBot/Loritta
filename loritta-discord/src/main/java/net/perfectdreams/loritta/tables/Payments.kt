package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.utils.exposed.rawJsonb
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import net.perfectdreams.loritta.tables.SonhosTransaction.nullable
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.dao.LongIdTable

object Payments : LongIdTable() {
    val userId = long("user").index()
    val gateway = enumeration("gateway", PaymentGateway::class)
    val reason = enumeration("reason", PaymentReason::class)
    val money = decimal("money", 12, 2).index()
    val createdAt = long("created_at")
    val paidAt = long("paid_at").nullable()
    val expiresAt = long("expires_at").nullable()
    val discount = double("discount").nullable()
    val metadata = rawJsonb("metadata", gson, jsonParser).nullable()
}