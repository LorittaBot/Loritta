package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

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
    val action: String, // TODO: use the enum
    val ticker: String,
    val sonhos: Long,
    val stockPrice: Long,
    val stockQuantity: Long
) : SonhosTransaction()

@Serializable
data class UnknownSonhosTransaction(
    override val id: Long,
    override val timestamp: Instant,
    override val user: UserId
) : SonhosTransaction()