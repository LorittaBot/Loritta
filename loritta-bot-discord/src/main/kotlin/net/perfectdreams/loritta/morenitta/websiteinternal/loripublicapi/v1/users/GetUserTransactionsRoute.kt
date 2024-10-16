package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.perfectdreams.loritta.common.utils.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.UserIdAsStringSerializer
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.*
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import net.perfectdreams.loritta.serializable.*
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class GetUserTransactionsRoute(m: LorittaBot) : LoriPublicAPIRoute(
    m,
    LoriPublicHttpApiEndpoints.GET_USER_TRANSACTIONS,
    RateLimitOptions(
        20,
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
        // However, we need to convert longs to strings because JAVASCRIPT SUCKS
        call.respondText(
            LoriPublicAPI.json.encodeToString(
                Result(
                    Json.encodeToJsonElement(convertToPublicTransactions(transactions)).jsonArray.map { modifyTypeFieldToOnlyHaveClassName(it) },
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

    // We parse the type fields to avoid exposing the entire package (which is useless)
    private fun modifyTypeFieldToOnlyHaveClassName(jsonElement: JsonElement): JsonObject {
        // Ensure the parsed element is a JsonObject
        if (jsonElement is JsonObject) {
            // Get the value of the "type" field
            val typeField = jsonElement["type"]?.jsonPrimitive?.content

            // Modify the "type" field using substringAfterLast
            val modifiedType = typeField?.substringAfterLast(".")

            // Create a new JsonObject with the modified "type" field
            val modifiedJsonObject = JsonObject(
                jsonElement.mapValues { (key, value) ->
                    if (key == "type") JsonPrimitive(modifiedType)
                    else value
                }
            )

            return modifiedJsonObject
        } else {
            throw IllegalArgumentException("Input JSON must be a JSON object")
        }
    }

    private fun convertToPublicTransactions(transactions: List<net.perfectdreams.loritta.serializable.SonhosTransaction>): List<SonhosTransaction> {
        return transactions.map { transaction ->
            when (transaction) {
                is net.perfectdreams.loritta.serializable.PaymentSonhosTransaction -> PaymentSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.givenBy,
                    transaction.receivedBy,
                    transaction.sonhos
                )
                is net.perfectdreams.loritta.serializable.DailyRewardSonhosTransaction -> DailyRewardSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos
                )
                is net.perfectdreams.loritta.serializable.BrokerSonhosTransaction -> BrokerSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.action,
                    transaction.ticker,
                    transaction.sonhos,
                    transaction.stockPrice,
                    transaction.stockQuantity
                )
                is net.perfectdreams.loritta.serializable.CoinFlipBetSonhosTransaction -> CoinFlipBetSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.winner,
                    transaction.loser,
                    transaction.quantity,
                    transaction.quantityAfterTax,
                    transaction.tax,
                    transaction.taxPercentage
                )
                is net.perfectdreams.loritta.serializable.CoinFlipBetGlobalSonhosTransaction -> CoinFlipBetGlobalSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.winner,
                    transaction.loser,
                    transaction.quantity,
                    transaction.quantityAfterTax,
                    transaction.tax,
                    transaction.taxPercentage,
                    transaction.timeOnQueue
                )
                is net.perfectdreams.loritta.serializable.EmojiFightBetSonhosTransaction -> EmojiFightBetSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.winner,
                    transaction.usersInMatch,
                    transaction.emoji,
                    transaction.entryPrice,
                    transaction.entryPriceAfterTax,
                    transaction.tax,
                    transaction.taxPercentage
                )
                is net.perfectdreams.loritta.serializable.SparklyPowerLSXSonhosTransaction -> SparklyPowerLSXSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.action,
                    transaction.sonhos,
                    transaction.sparklyPowerSonhos,
                    transaction.playerName,
                    transaction.playerUniqueId,
                    transaction.exchangeRate
                )
                is net.perfectdreams.loritta.serializable.DailyTaxSonhosTransaction -> DailyTaxSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.maxDayThreshold,
                    transaction.minimumSonhosForTrigger
                )
                is net.perfectdreams.loritta.serializable.SonhosBundlePurchaseSonhosTransaction -> SonhosBundlePurchaseSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos
                )
                is net.perfectdreams.loritta.serializable.DivineInterventionSonhosTransaction -> DivineInterventionSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.action,
                    // transaction.givenBy,
                    transaction.sonhos,
                    transaction.reason
                )
                is net.perfectdreams.loritta.serializable.BotVoteSonhosTransaction -> BotVoteSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.websiteSource,
                    transaction.sonhos
                )
                is net.perfectdreams.loritta.serializable.Christmas2022SonhosTransaction -> Christmas2022SonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.gifts
                )
                is net.perfectdreams.loritta.serializable.Easter2023SonhosTransaction -> Easter2023SonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.baskets
                )
                is net.perfectdreams.loritta.serializable.ShipEffectSonhosTransaction -> ShipEffectSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos
                )
                is net.perfectdreams.loritta.serializable.RaffleRewardSonhosTransaction -> RaffleRewardSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.quantity,
                    transaction.quantityAfterTax,
                    transaction.tax,
                    transaction.taxPercentage
                )
                is net.perfectdreams.loritta.serializable.RaffleTicketsSonhosTransaction -> RaffleTicketsSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.ticketQuantity
                )
                is net.perfectdreams.loritta.serializable.PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction -> PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.liveId,
                    transaction.streamId
                )
                is net.perfectdreams.loritta.serializable.PowerStreamClaimedFirstSonhosRewardSonhosTransaction -> PowerStreamClaimedFirstSonhosRewardSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.liveId,
                    transaction.streamId
                )
                is net.perfectdreams.loritta.serializable.LoriCoolCardsBoughtBoosterPackSonhosTransaction -> LoriCoolCardsBoughtBoosterPackSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.eventId
                )
                is net.perfectdreams.loritta.serializable.LoriCoolCardsFinishedAlbumSonhosTransaction -> LoriCoolCardsFinishedAlbumSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.eventId
                )
                is net.perfectdreams.loritta.serializable.LoriCoolCardsPaymentSonhosTradeTransaction -> LoriCoolCardsPaymentSonhosTradeTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.givenBy,
                    transaction.receivedBy,
                    transaction.sonhos
                )
                is net.perfectdreams.loritta.serializable.LorittaItemShopBoughtBackgroundTransaction -> LorittaItemShopBoughtBackgroundTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.internalBackgroundId
                )
                is net.perfectdreams.loritta.serializable.LorittaItemShopBoughtProfileDesignTransaction -> LorittaItemShopBoughtProfileDesignTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.internalProfileDesignId
                )
                is net.perfectdreams.loritta.serializable.BomDiaECiaCallCalledTransaction -> BomDiaECiaCallCalledTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos
                )
                is net.perfectdreams.loritta.serializable.BomDiaECiaCallWonTransaction -> BomDiaECiaCallWonTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos
                )
                is net.perfectdreams.loritta.serializable.GarticosTransferTransaction -> GarticosTransferTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user,
                    transaction.sonhos,
                    transaction.garticos,
                    transaction.transferRate
                )
                is net.perfectdreams.loritta.serializable.UnknownSonhosTransaction -> UnknownSonhosTransaction(
                    transaction.id,
                    transaction.transactionType,
                    transaction.timestamp,
                    transaction.user
                )
            }
        }
    }

    @Serializable
    sealed class SonhosTransaction {
        abstract val id: Long
        abstract val transactionType: TransactionType
        abstract val timestamp: kotlinx.datetime.Instant
        abstract val user: UserId
    }

    @Serializable
    data class PaymentSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val givenBy: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val receivedBy: UserId,
        val sonhos: Long
    ) : SonhosTransaction()

    @Serializable
    data class DailyRewardSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long
    ) : SonhosTransaction()

    @Serializable
    data class BrokerSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val action: LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction,
        val ticker: String,
        val sonhos: Long,
        val stockPrice: Long,
        val stockQuantity: Long
    ) : SonhosTransaction()

    @Serializable
    data class CoinFlipBetSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val winner: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val loser: UserId,
        val quantity: Long,
        val quantityAfterTax: Long,
        val tax: Long?,
        val taxPercentage: Double?
    ) : SonhosTransaction()

    @Serializable
    data class CoinFlipBetGlobalSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val winner: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val loser: UserId,
        val quantity: Long,
        val quantityAfterTax: Long,
        val tax: Long?,
        val taxPercentage: Double?,
        val timeOnQueue: Long
    ) : SonhosTransaction()

    @Serializable
    data class EmojiFightBetSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val winner: UserId,
        val usersInMatch: Long,
        val emoji: String,
        val entryPrice: Long,
        val entryPriceAfterTax: Long,
        val tax: Long?,
        val taxPercentage: Double?
    ) : SonhosTransaction()

    @Serializable
    data class SparklyPowerLSXSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val action: SparklyPowerLSXTransactionEntryAction,
        val sonhos: Long,
        val sparklyPowerSonhos: Long,
        val playerName: String,
        val playerUniqueId: String, // TODO: This is an UUID but Kotlin doesn't have an mpp UUID class yet
        val exchangeRate: Double
    ) : SonhosTransaction()

    @Serializable
    data class DailyTaxSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val maxDayThreshold: Int,
        val minimumSonhosForTrigger: Long
    ) : SonhosTransaction()

    @Serializable
    data class SonhosBundlePurchaseSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long
    ) : SonhosTransaction()

    @Serializable
    data class DivineInterventionSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val action: DivineInterventionTransactionEntryAction,
        // No need to expose this
        // @Serializable(UserIdAsStringSerializer::class)
        // val givenBy: UserId?,
        val sonhos: Long,
        val reason: String?
    ) : SonhosTransaction()

    @Serializable
    data class BotVoteSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val websiteSource: WebsiteVoteSource,
        val sonhos: Long
    ) : SonhosTransaction()

    @Serializable
    data class Christmas2022SonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val gifts: Int
    ) : SonhosTransaction()

    @Serializable
    data class Easter2023SonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val baskets: Int
    ) : SonhosTransaction()

    @Serializable
    data class ShipEffectSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long
    ) : SonhosTransaction()

    @Serializable
    data class RaffleRewardSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val quantity: Long,
        val quantityAfterTax: Long,
        val tax: Long?,
        val taxPercentage: Double?
    ) : SonhosTransaction()

    @Serializable
    data class RaffleTicketsSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val ticketQuantity: Int
    ) : SonhosTransaction()

    @Serializable
    data class PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val liveId: String,
        val streamId: Long
    ) : SonhosTransaction()

    @Serializable
    data class PowerStreamClaimedFirstSonhosRewardSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val liveId: String,
        val streamId: Long
    ) : SonhosTransaction()

    @Serializable
    data class LoriCoolCardsBoughtBoosterPackSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val eventId: Long
    ) : SonhosTransaction()

    @Serializable
    data class LoriCoolCardsFinishedAlbumSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val eventId: Long
    ) : SonhosTransaction()

    @Serializable
    data class LoriCoolCardsPaymentSonhosTradeTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val givenBy: UserId,
        @Serializable(UserIdAsStringSerializer::class)
        val receivedBy: UserId,
        val sonhos: Long
    ) : SonhosTransaction()

    @Serializable
    data class LorittaItemShopBoughtProfileDesignTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val internalProfileDesignId: String,
    ) : SonhosTransaction()

    @Serializable
    data class LorittaItemShopBoughtBackgroundTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val internalBackgroundId: String
    ) : SonhosTransaction()

    @Serializable
    data class BomDiaECiaCallCalledTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long
    ) : SonhosTransaction()

    @Serializable
    data class BomDiaECiaCallWonTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long
    ) : SonhosTransaction()

    @Serializable
    data class GarticosTransferTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId,
        val sonhos: Long,
        val garticos: Long,
        val transferRate: Double
    ) : SonhosTransaction()

    @Serializable
    data class UnknownSonhosTransaction(
        override val id: Long,
        override val transactionType: TransactionType,
        override val timestamp: kotlinx.datetime.Instant,
        @Serializable(UserIdAsStringSerializer::class)
        override val user: UserId
    ) : SonhosTransaction()

    @Serializable
    data class Result(
        val transactions: List<JsonObject>,
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