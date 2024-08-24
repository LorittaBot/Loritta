package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.*
import net.perfectdreams.loritta.serializable.SonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class GetUserTransactionsRoute(m: LorittaBot) : LoriPublicAPIRoute(
    m,
    "/users/{userId}/transactions",
    RateLimitOptions(
        5,
        5.seconds
    )
) {
    override suspend fun onAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo) {
        val userId = call.parameters.getOrFail("userId").toLong()
        val transactionTypes = call.parameters["transactionTypes"]
            ?.split(",")
            ?.map {
                try {
                    TransactionType.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    call.respondJson(
                        Json.encodeToString(
                            GenericErrorResponse(
                                "Unknown Transaction Type $it"
                            )
                        ),
                        status = HttpStatusCode.BadRequest
                    )
                    return
                }
            } ?: TransactionType.entries

        val limit = call.parameters["limit"]?.toInt() ?: 10
        if (limit !in 1..100) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Limit is not in valid range (1..100)"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }
        val offset = (call.parameters["offset"]?.toLong()) ?: 0
        val beforeDate = call.parameters["beforeDate"]?.let { Instant.parse(it) }
        val afterDate = call.parameters["afterDate"]?.let { Instant.parse(it) }

        val (totalTransactions, transactions) = m.pudding.transaction {
            val total = m.pudding.sonhos.getUserTotalTransactions(
                UserId(userId),
                transactionTypes,
                beforeDate,
                afterDate
            )

            val transactions = m.pudding.sonhos.getUserTransactions(
                UserId(userId),
                transactionTypes,
                limit,
                offset,
                beforeDate,
                afterDate
            )

            Pair(total, transactions)
        }

        // We send the internal transaction because it would take veeeery long to implement all of them
        // Also because *technically* the "type" of the transaction can be used to filter transactions too (they are very useful for that)
        call.respondText(
            LoriPublicAPI.json.encodeToString(
                Result(
                    transactions,
                    Result.Paging(
                        totalTransactions,
                        limit,
                        offset
                    )
                )
            ),
            ContentType.Application.Json
        )
    }

    @Serializable
    data class Result(
        val transactions: List<SonhosTransaction>,
        val paging: Paging
    ) {
        @Serializable
        data class Paging(
            val total: Long,
            val limit: Int,
            val offset: Long
        )
    }
}