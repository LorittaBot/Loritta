package net.perfectdreams.loritta.utils

import net.perfectdreams.loritta.tables.SonhosTransaction
import org.jetbrains.exposed.sql.insert
import java.lang.RuntimeException

object PaymentUtils {
    var economyEnabled = true

    fun addToTransactionLogNested(
            quantity: Long,
            reason: SonhosPaymentReason,
            receivedBy: Long? = null,
            givenBy: Long? = null,
            givenAtMillis: Long = System.currentTimeMillis()
    ) {
        if (receivedBy == null && givenBy == null)
            throw IllegalArgumentException("receivedBy and givenBy is null! One of them must NOT be null!")

        SonhosTransaction.insert {
            it[SonhosTransaction.givenBy] = givenBy
            it[SonhosTransaction.receivedBy] = receivedBy
            it[givenAt] = givenAtMillis
            it[SonhosTransaction.quantity] = quantity.toBigDecimal()
            it[SonhosTransaction.reason] = reason
        }
    }

    class EconomyDisabledException : RuntimeException()
}