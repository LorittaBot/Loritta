package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import kotlin.math.ceil

class PaymentsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getActiveMoneyFromDonations(userId: UserId) = pudding.transaction {
        _getActiveMoneyFromDonations(userId)
    }

    internal fun _getActiveMoneyFromDonations(userId: UserId): Double {
        // This is a weird workaround that fixes users complaining that 19.99 + 19.99 != 40 (it equals to 39.98)
        return ceil(
            Payments.select {
                (Payments.expiresAt greaterEq System.currentTimeMillis()) and
                        (Payments.reason eq PaymentReason.DONATION) and
                        (Payments.userId eq userId.value.toLong())
            }.sumOf {
                ceil(it[Payments.money].toDouble())
            }
        )
    }
}