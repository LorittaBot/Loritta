package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.DivineInterventionTransactionEntryAction
import net.perfectdreams.loritta.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.common.utils.SparklyPowerLSXTransactionEntryAction
import net.perfectdreams.loritta.common.utils.WebsiteVoteSource

@Serializable
sealed class StoredSonhosTransaction

@Serializable
data class StoredEmojiFightBetSonhosTransaction(
    val emojiFightMatchmakingResultsId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredShipEffectSonhosTransaction(
    val shipEffectId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredDailyRewardSonhosTransaction(
    val dailyId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredDivineInterventionSonhosTransaction(
    val action: DivineInterventionTransactionEntryAction,
    val editedBy: Long,
    val reason: String?
) : StoredSonhosTransaction()

@Serializable
data class StoredBotVoteSonhosTransaction(val websiteSource: WebsiteVoteSource) : StoredSonhosTransaction()

@Serializable
data class StoredDailyTaxSonhosTransaction(
    val maxDayThreshold: Int,
    val minimumSonhosForTrigger: Long,
    val tax: Double
) : StoredSonhosTransaction()

@Serializable
data class StoredPaymentSonhosTransaction(
    val givenBy: Long,
    val receivedBy: Long,
    val paymentResultId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredBrokerSonhosTransaction(
    val action: LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction,
    val ticker: String,
    val stockPrice: Long,
    val stockQuantity: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredSonhosBundlePurchaseTransaction(val bundleId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredRaffleRewardTransaction(val raffleId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredRaffleTicketsTransaction(val raffleId: Long, val ticketQuantity: Int) : StoredSonhosTransaction()

@Serializable
data class StoredCoinFlipBetTransaction(val matchmakingResultId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredCoinFlipBetGlobalTransaction(val matchmakingResultId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredSparklyPowerLSXSonhosTransaction(
    val action: SparklyPowerLSXTransactionEntryAction,
    val sparklyPowerSonhos: Long,
    val playerName: String,
    val playerUniqueId: String,
    val exchangeRate: Double
) : StoredSonhosTransaction()

@Serializable
data class StoredChristmas2022SonhosTransaction(val gifts: Int) : StoredSonhosTransaction()

@Serializable
data class StoredEaster2023SonhosTransaction(val baskets: Int) : StoredSonhosTransaction()

@Serializable
data class StoredPowerStreamClaimedLimitedTimeSonhosRewardSonhosTransaction(
    val liveId: String,
    val streamId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredPowerStreamClaimedFirstSonhosRewardSonhosTransaction(
    val liveId: String,
    val streamId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredLoriCoolCardsBoughtBoosterPackSonhosTransaction(
    val eventId: Long,
    val boosterPackId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredLoriCoolCardsFinishedAlbumSonhosTransaction(
    val eventId: Long,
    val completionId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredLoriCoolCardsPaymentSonhosTradeTransaction(
    val givenBy: Long,
    val receivedBy: Long,
    val figurittasTradeId: Long
) : StoredSonhosTransaction()