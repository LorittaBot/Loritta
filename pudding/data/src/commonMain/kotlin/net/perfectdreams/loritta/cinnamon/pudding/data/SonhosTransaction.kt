package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.common.utils.SparklyPowerLSXTransactionEntryAction

@Serializable
sealed class SonhosTransaction {
    abstract val id: Long
    abstract val timestamp: Instant
    abstract val user: UserId
}

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
data class CoinflipBetGlobalSonhosTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId,
    val winner: UserId,
    val loser: UserId,
    val quantity: Long,
    val timeOnQueue: Long
) : SonhosTransaction()

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
data class UnknownSonhosTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId
) : SonhosTransaction()