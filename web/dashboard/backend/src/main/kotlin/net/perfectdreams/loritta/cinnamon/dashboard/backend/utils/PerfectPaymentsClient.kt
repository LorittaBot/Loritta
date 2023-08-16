package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentGateway
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import org.jetbrains.exposed.sql.insert
import java.util.*

class PerfectPaymentsClient(val m: LorittaDashboardBackend, val url: String) {
    companion object {
        private val http = HttpClient {}
    }

    private val logger = KotlinLogging.logger {}

    /**
     * Creates a payment in PerfectPayments, creates a entry in Loritta's payment table and returns the payment URL
     *
     * @return the payment URL
     */
    suspend fun createPayment(
        userId: Long,
        paymentTitle: String,
        amount: Long,
        storedAmount: Long,
        paymentReason: PaymentReason,
        externalReference: String,
        discount: Double? = null,
        metadata: JsonObject? = null
    ): String {
        logger.info { "Requesting PerfectPayments payment URL for $userId" }
        val payments = http.post("${url}api/v1/payments") {
            header("Authorization", m.config.perfectPayments.token)

            setBody(
                TextContent(
                    Json.encodeToString(
                        CreatePaymentRequest(
                            paymentTitle,
                            "${m.config.legacyDashboardUrl.removeSuffix("/")}/api/v1/callbacks/perfect-payments",
                            amount,
                            "BRL",
                            externalReference
                        )
                    ),
                    ContentType.Application.Json
                )
            )
        }

        println(payments.status)
        val paymentBody = payments.bodyAsText()
        println(paymentBody)

        val paymentResponse = Json.parseToJsonElement(paymentBody)
            .jsonObject

        val partialPaymentId = UUID.fromString(paymentResponse["id"]!!.jsonPrimitive.content)
        val paymentUrl = paymentResponse["paymentUrl"]!!.jsonPrimitive.content

        logger.info { "Payment successfully created for $userId! ID: $partialPaymentId" }
        m.pudding.transaction {
            Payments.insert {
                it[Payments.userId] = userId
                it[Payments.gateway] = PaymentGateway.PERFECTPAYMENTS
                it[Payments.reason] = paymentReason
                it[Payments.discount] = discount
                it[Payments.metadata] = Json.encodeToString(metadata)
                it[Payments.money] = (storedAmount.toDouble() / 100).toBigDecimal()
                it[Payments.createdAt] = System.currentTimeMillis()
                it[Payments.referenceId] = partialPaymentId
            }
        }

        return paymentUrl
    }

    @Serializable
    data class CreatePaymentRequest(
        val title: String,
        val callbackUrl: String,
        val amount: Long,
        val currencyId: String,
        val externalReference: String
    )
}