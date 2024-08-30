package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.*

@Serializable
sealed class SonhosTransaction {
    abstract val id: Long
    abstract val transactionType: TransactionType
    abstract val timestamp: Instant
    abstract val user: UserId
}

@Serializable
data class PaymentSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val givenBy: UserId,
    val receivedBy: UserId,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class DailyRewardSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class BrokerSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
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
    override val timestamp: Instant,
    override val user: UserId,
    val winner: UserId,
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
    override val timestamp: Instant,
    override val user: UserId,
    val winner: UserId,
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
    override val timestamp: Instant,
    override val user: UserId,
    val matchmakingId: Long,
    val matchId: Long?,
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
    override val timestamp: Instant,
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
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val maxDayThreshold: Int,
    val minimumSonhosForTrigger: Long
) : SonhosTransaction()

@Serializable
data class SonhosBundlePurchaseSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class DivineInterventionSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val action: DivineInterventionTransactionEntryAction,
    val givenBy: UserId?,
    val sonhos: Long,
    val reason: String?
) : SonhosTransaction()

@Serializable
data class BotVoteSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val websiteSource: WebsiteVoteSource,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class Christmas2022SonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val gifts: Int
) : SonhosTransaction()

@Serializable
data class Easter2023SonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val baskets: Int
) : SonhosTransaction()

@Serializable
data class ShipEffectSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class RaffleRewardSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
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
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val ticketQuantity: Int
) : SonhosTransaction()

@Serializable
data class PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val liveId: String,
    val streamId: Long
) : SonhosTransaction()

@Serializable
data class PowerStreamClaimedFirstSonhosRewardSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val liveId: String,
    val streamId: Long
) : SonhosTransaction()

@Serializable
data class LoriCoolCardsBoughtBoosterPackSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val eventId: Long
) : SonhosTransaction()

@Serializable
data class LoriCoolCardsFinishedAlbumSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val eventId: Long
) : SonhosTransaction()

@Serializable
data class LoriCoolCardsPaymentSonhosTradeTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId,
    val givenBy: UserId,
    val receivedBy: UserId,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class UnknownSonhosTransaction(
    override val id: Long,
    override val transactionType: TransactionType,
    override val timestamp: Instant,
    override val user: UserId
) : SonhosTransaction()