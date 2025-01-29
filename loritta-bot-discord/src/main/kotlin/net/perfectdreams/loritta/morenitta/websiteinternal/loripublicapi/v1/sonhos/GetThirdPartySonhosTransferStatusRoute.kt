package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.sonhos

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.ThirdPartySonhosTransferRequests
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.*
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import kotlin.time.Duration.Companion.seconds

class GetThirdPartySonhosTransferStatusRoute(m: LorittaBot) : LoriPublicAPIRoute(
    m,
    LoriPublicHttpApiEndpoints.GET_THIRD_PARTY_SONHOS_TRANSFER_STATUS,
    RateLimitOptions(
        5,
        1.seconds
    )
) {
    override suspend fun onAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo) {
        val sonhosTransferId = call.parameters["sonhosTransferId"]!!.toLong()

        val sonhosTransferResult = m.transaction {
            ThirdPartySonhosTransferRequests.selectAll()
                .where {
                    ThirdPartySonhosTransferRequests.id eq sonhosTransferId and (ThirdPartySonhosTransferRequests.tokenUser eq tokenInfo.userId)
                }
                .firstOrNull()
        }

        if (sonhosTransferResult == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Sonhos Transaction"
                    )
                ),
                status = HttpStatusCode.NotFound
            )
            return
        }

         call.respondText(
            LoriPublicAPI.json.encodeToString(
                Result(
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.giver],
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.giverAcceptedAt]?.toKotlinInstant(),
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.receiver],
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.receiverAcceptedAt]?.toKotlinInstant(),
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.quantity],
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.quantity] - sonhosTransferResult[ThirdPartySonhosTransferRequests.tax],
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.tax],
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.taxPercentage],
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.requestedAt].toKotlinInstant(),
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.expiresAt].toKotlinInstant(),
                    sonhosTransferResult[ThirdPartySonhosTransferRequests.transferredAt]?.toKotlinInstant(),
                )
            ),
            ContentType.Application.Json
        )
    }

    @Serializable
    data class Result(
        @Serializable(LongAsStringSerializer::class)
        val giverId: Long,
        val giverAcceptedAt: Instant?,
        @Serializable(LongAsStringSerializer::class)
        val receiverId: Long,
        val receiverAcceptedAt: Instant?,
        val quantity: Long,
        val quantityAfterTax: Long,
        val tax: Long,
        val taxPercentage: Double,
        val requestedAt: Instant?,
        val expiresAt: Instant?,
        val transferredAt: Instant?,
    )
}