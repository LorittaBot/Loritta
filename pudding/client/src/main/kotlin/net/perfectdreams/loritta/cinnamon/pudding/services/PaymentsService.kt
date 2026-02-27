package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class PaymentsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getActiveMoneyFromDonations(userId: UserId) = pudding.transaction {
        return@transaction Payments.selectAll().where {
            (Payments.expiresAt greaterEq System.currentTimeMillis()) and
                    (Payments.reason eq PaymentReason.DONATION) and
                    (Payments.userId eq userId.value.toLong())
        }.sumOf {
            it[Payments.money].toInt()
        }
    }
}
