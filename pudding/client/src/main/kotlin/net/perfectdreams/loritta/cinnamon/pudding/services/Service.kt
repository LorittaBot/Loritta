package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Achievement
import net.perfectdreams.loritta.cinnamon.pudding.data.Background
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.Daily
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.CoinFlipBetGlobalSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.CoinFlipBetSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.Daily
import net.perfectdreams.loritta.cinnamon.pudding.data.DailyTaxSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.DefaultBackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.GuildProfile
import net.perfectdreams.loritta.cinnamon.pudding.data.DivineInterventionSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.Marriage
import net.perfectdreams.loritta.cinnamon.pudding.data.PatchNotesNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.PaymentSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.ProfileDesignGroupBackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.ProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.data.Rectangle
import net.perfectdreams.loritta.cinnamon.pudding.data.Reputation
import net.perfectdreams.loritta.cinnamon.pudding.data.ServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.data.ShipEffect
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosBundlePurchaseSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.SparklyPowerLSXSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.data.UserProfile
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingDaily
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingGuildProfile
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingReputation
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingShipEffect
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundVariations
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.BrokerSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyTaxSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.DivineInterventionSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.GuildProfiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.tables.PatchNotesNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundlePurchaseSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.SparklyPowerLSXSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import org.jetbrains.exposed.sql.ResultRow

open class Service(private val pudding: Pudding) {
    fun PuddingUserProfile.Companion.fromRow(row: ResultRow) = PuddingUserProfile(
        pudding,
        UserProfile(
            UserId(row[Profiles.id].value.toULong()),
            row[Profiles.settings].value,
            row[Profiles.money],
            row[Profiles.isAfk],
            row[Profiles.afkReason],
            row[Profiles.marriage]?.value
        )
    )

    fun PuddingProfileSettings.Companion.fromRow(row: ResultRow) = PuddingProfileSettings(
        pudding,
        ProfileSettings(
            UserId(row[UserSettings.id].value.toULong()),
            row[UserSettings.aboutMe],
            row[UserSettings.gender],
            row[UserSettings.activeBackground]?.value,
            row[UserSettings.activeProfileDesign]?.value,
            row[UserSettings.doNotSendXpNotificationsInDm],
            row[UserSettings.discordAccountFlags],
            row[UserSettings.discordPremiumType],
            row[UserSettings.language]
        )
    )

    fun PuddingShipEffect.Companion.fromRow(row: ResultRow) = PuddingShipEffect(
        pudding,
        ShipEffect(
            row[ShipEffects.id].value,
            UserId(row[ShipEffects.buyerId].toULong()),
            UserId(row[ShipEffects.user1Id].toULong()),
            UserId(row[ShipEffects.user2Id].toULong()),
            row[ShipEffects.editedShipValue],
            Instant.fromEpochMilliseconds(row[ShipEffects.expiresAt])
        )
    )

    fun PuddingMarriage.Companion.fromRow(row: ResultRow) = PuddingMarriage(
        pudding,
        Marriage(
            row[Marriages.id].value,
            UserId(row[Marriages.user1].toULong()),
            UserId(row[Marriages.user2].toULong()),
            Instant.fromEpochMilliseconds(row[Marriages.marriedSince])
        )
    )

    fun PuddingServerConfigRoot.Companion.fromRow(row: ResultRow) = PuddingServerConfigRoot(
        pudding,
        ServerConfigRoot(
            row[ServerConfigs.id].value.toULong(),
            row[ServerConfigs.localeId]
        )
    )

    fun PuddingAchievement.Companion.fromRow(row: ResultRow) = PuddingAchievement(
        pudding,
        Achievement(
            UserId(row[UserAchievements.user].value.toULong()),
            row[UserAchievements.type],
            row[UserAchievements.achievedAt].toKotlinInstant()
        )
    )

    fun PuddingBackground.Companion.fromRow(row: ResultRow) = PuddingBackground(
        pudding,
        Background.fromRow(row)
    )

    fun PuddingGuildProfile.Companion.fromRow(row: ResultRow) = PuddingGuildProfile(
        pudding,
        GuildProfile(
            row[GuildProfiles.guildId],
            row[GuildProfiles.userId],
            row[GuildProfiles.xp],
            row[GuildProfiles.quickPunishment],
            // row[GuildProfiles.money],
            row[GuildProfiles.isInGuild]
        )
    )

    fun PuddingReputation.Companion.fromRow(row: ResultRow) = PuddingReputation(
        pudding,
        Reputation(
            row[Reputations.givenById],
            row[Reputations.givenByIp],
            row[Reputations.givenByEmail],
            row[Reputations.receivedById],
            row[Reputations.receivedAt],
            row[Reputations.content]
        )
    )

    fun PuddingDaily.Companion.fromRow(row: ResultRow) = PuddingDaily(
        pudding,
        Daily(
            row[Dailies.receivedById],
            row[Dailies.receivedAt],
            row[Dailies.ip],
            row[Dailies.email],
            row[Dailies.userAgent]
        )
    )
}

fun Background.Companion.fromRow(row: ResultRow) = Background(
    row[Backgrounds.id].value,
    row[Backgrounds.enabled],
    row[Backgrounds.rarity],
    row[Backgrounds.createdBy].toList(),
    row[Backgrounds.set]?.value
)

fun BackgroundVariation.Companion.fromRow(row: ResultRow): BackgroundVariation {
    val groupId = row[BackgroundVariations.profileDesignGroup]?.value

    // Must be "getOrNull" because the columns are non-nullable, but we are doing a left join, so it may not be present in the ResultRow set!
    val cropAsJson = row[BackgroundVariations.crop]
    val crop = if (cropAsJson != null) {
        Json.decodeFromString<Rectangle>(cropAsJson)
    } else null

    return if (groupId == null)
        DefaultBackgroundVariation(
            row[BackgroundVariations.file],
            row[BackgroundVariations.preferredMediaType],
            crop
        )
    else
        ProfileDesignGroupBackgroundVariation(
            groupId.toString(),
            row[BackgroundVariations.file],
            row[BackgroundVariations.preferredMediaType],
            crop
        )
}

fun SonhosTransaction.Companion.fromRow(row: ResultRow): SonhosTransaction {
    // "hasValue" does not work, because it only checks if the value is present on the table BUT it is always present! (but it is null)
    return if (row.getOrNull(PaymentSonhosTransactionsLog.id) != null) {
        PaymentSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            UserId(row[PaymentSonhosTransactionResults.givenBy].value),
            UserId(row[PaymentSonhosTransactionResults.receivedBy].value),
            row[PaymentSonhosTransactionResults.sonhos],
        )
    } else if (row.getOrNull(BrokerSonhosTransactionsLog.id) != null) {
        BrokerSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[BrokerSonhosTransactionsLog.action],
            row[BrokerSonhosTransactionsLog.ticker].value,
            row[BrokerSonhosTransactionsLog.sonhos],
            row[BrokerSonhosTransactionsLog.stockPrice],
            row[BrokerSonhosTransactionsLog.stockQuantity]
        )
    } else if (row.getOrNull(CoinFlipBetGlobalSonhosTransactionsLog.id) != null) {
        CoinFlipBetGlobalSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            UserId(row[CoinFlipBetGlobalMatchmakingResults.winner].value),
            UserId(row[CoinFlipBetGlobalMatchmakingResults.loser].value),
            row[CoinFlipBetGlobalMatchmakingResults.quantity],
            row[CoinFlipBetGlobalMatchmakingResults.quantityAfterTax],
            row[CoinFlipBetGlobalMatchmakingResults.tax],
            row[CoinFlipBetGlobalMatchmakingResults.taxPercentage],
            row[CoinFlipBetGlobalMatchmakingResults.timeOnQueue].toMillis(),
        )
    } else if (row.getOrNull(CoinFlipBetSonhosTransactionsLog.id) != null) {
        CoinFlipBetSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            UserId(row[CoinFlipBetMatchmakingResults.winner].value),
            UserId(row[CoinFlipBetMatchmakingResults.loser].value),
            row[CoinFlipBetMatchmakingResults.quantity],
            row[CoinFlipBetMatchmakingResults.quantityAfterTax],
            row[CoinFlipBetMatchmakingResults.tax],
            row[CoinFlipBetMatchmakingResults.taxPercentage]
        )
    } else if (row.getOrNull(SparklyPowerLSXSonhosTransactionsLog.id) != null) {
        SparklyPowerLSXSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[SparklyPowerLSXSonhosTransactionsLog.action],
            row[SparklyPowerLSXSonhosTransactionsLog.sonhos],
            row[SparklyPowerLSXSonhosTransactionsLog.sparklyPowerSonhos],
            row[SparklyPowerLSXSonhosTransactionsLog.playerName],
            row[SparklyPowerLSXSonhosTransactionsLog.playerUniqueId].toString(),
            row[SparklyPowerLSXSonhosTransactionsLog.exchangeRate]
        )
    } else if (row.getOrNull(DailyTaxSonhosTransactionsLog.id) != null) {
        DailyTaxSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[DailyTaxSonhosTransactionsLog.sonhos],
            row[DailyTaxSonhosTransactionsLog.maxDayThreshold],
            row[DailyTaxSonhosTransactionsLog.minimumSonhosForTrigger]
        )
    } else if (row.getOrNull(SonhosBundlePurchaseSonhosTransactionsLog.id) != null) {
        SonhosBundlePurchaseSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[SonhosBundles.sonhos]
        )
    } else if (row.getOrNull(DivineInterventionSonhosTransactionsLog.id) != null) {
        DivineInterventionSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[DivineInterventionSonhosTransactionsLog.action],
            row[DivineInterventionSonhosTransactionsLog.editedBy]?.let { UserId(it.value) },
            row[DivineInterventionSonhosTransactionsLog.sonhos],
            row[DivineInterventionSonhosTransactionsLog.reason]
        )
    } else {
        UnknownSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
        )
    }
}

fun Daily.Companion.fromRow(row: ResultRow) = Daily(
    UserId(row[Dailies.receivedById]),
    Instant.fromEpochMilliseconds(row[Dailies.receivedAt])
)

fun PatchNotesNotification.Companion.fromRow(row: ResultRow) = PatchNotesNotification(
    row[PatchNotesNotifications.path]
)