package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import net.perfectdreams.loritta.cinnamon.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.utils.SparklyPowerLSXTransactionEntryAction
import net.perfectdreams.loritta.cinnamon.utils.WebsiteVoteSource

@Serializable
sealed class SonhosTransaction {
    abstract val id: Long
    abstract val timestamp: Instant
    abstract val user: UserId
}

@Serializable
data class PaymentSonhosTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val givenBy: UserId,
    val receivedBy: UserId,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class BrokerSonhosTransaction(
    override val id: Long,
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
    override val timestamp: Instant,
    override val user: UserId,
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
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long,
    val maxDayThreshold: Int,
    val minimumSonhosForTrigger: Long
) : SonhosTransaction()

@Serializable
data class SonhosBundlePurchaseSonhosTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class DivineInterventionSonhosTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val action: DivineInterventionTransactionEntryAction,
    val givenBy: UserId?,
    val sonhos: Long,
    val reason: String?
) : SonhosTransaction()

@Serializable
data class BotVoteTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val websiteSource: WebsiteVoteSource,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class ShipEffectSonhosTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val sonhos: Long
) : SonhosTransaction()

@Serializable
data class UnknownSonhosTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId
) : SonhosTransaction()