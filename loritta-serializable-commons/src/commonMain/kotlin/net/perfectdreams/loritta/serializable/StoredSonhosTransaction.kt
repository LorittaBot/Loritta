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
data class StoredAPIInitiatedPaymentSonhosTransaction(
    val givenBy: Long,
    val receivedBy: Long,
    val paymentResultId: Long,
    val reason: String
) : StoredSonhosTransaction()

// This is deprecated
@Serializable
data class StoredThirdPartyPaymentSonhosTransaction(
    val thirdPartyPaymentId: Long
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
data class StoredReactionEventSonhosTransaction(val eventInternalId: String, val craftedCount: Int) : StoredSonhosTransaction()

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

@Serializable
data class StoredLorittaItemShopBoughtProfileDesignTransaction(
    val internalProfileDesignId: String
) : StoredSonhosTransaction()

@Serializable
data class StoredLorittaItemShopBoughtBackgroundTransaction(
    val internalBackgroundId: String
) : StoredSonhosTransaction()

@Serializable
data class StoredLorittaItemShopComissionProfileDesignTransaction(
    val boughtUserId: Long,
    val internalProfileDesignId: String
) : StoredSonhosTransaction()

@Serializable
data class StoredLorittaItemShopComissionBackgroundTransaction(
    val boughtUserId: Long,
    val internalBackgroundId: String
) : StoredSonhosTransaction()

@Serializable
data object StoredBomDiaECiaCallCalledTransaction : StoredSonhosTransaction()

@Serializable
data object StoredBomDiaECiaCallWonTransaction : StoredSonhosTransaction()

@Serializable
data class StoredGarticosTransferTransaction(
    val garticos: Long,
    val transferRate: Double
) : StoredSonhosTransaction()

@Serializable
data class StoredMarriageMarryTransaction(val marriedWithUserId: Long) : StoredSonhosTransaction()

@Serializable
data object StoredMarriageRestoreTransaction : StoredSonhosTransaction()

@Serializable
data object StoredMarriageRestoreAutomaticTransaction : StoredSonhosTransaction()

@Serializable
data object StoredMarriageLoveLetterTransaction : StoredSonhosTransaction()

@Serializable
data class StoredChargebackedSonhosBundleTransaction(val triggeredByUserId: Long) : StoredSonhosTransaction()

@Serializable
data object StoredVacationModeLeaveTransaction : StoredSonhosTransaction()

@Serializable
data class StoredReputationDeletedTransaction(val reputationId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredBlackjackPayoutTransaction(val matchId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredBlackjackTiedTransaction(val matchId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredBlackjackInsuranceTransaction(val matchId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredBlackjackInsurancePayoutTransaction(val matchId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredBlackjackJoinedTransaction(val matchId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredBlackjackSplitTransaction(val matchId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredBlackjackDoubleDownTransaction(val matchId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredBlackjackRefundTransaction(val matchId: Long) : StoredSonhosTransaction()

@Serializable
data class StoredDropChatTransaction(
    val dropId: Long,
    val charged: Boolean,
    val givenById: Long?,
    val receivedById: Long,
    val guildId: Long
) : StoredSonhosTransaction()

@Serializable
data class StoredDropCallTransaction(
    val dropId: Long,
    val charged: Boolean,
    val givenById: Long?,
    val receivedById: Long,
    val guildId: Long
) : StoredSonhosTransaction()