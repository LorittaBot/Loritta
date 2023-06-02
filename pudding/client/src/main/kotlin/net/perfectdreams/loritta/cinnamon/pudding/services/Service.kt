package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.*
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingReputation
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingShipEffect
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundVariations
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetGlobalSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.tables.PatchNotesNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.PaymentSonhosTransactionResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackagesEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.CorreiosPackageUpdateUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxTaxedUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxWarnUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.raffles.Raffles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.InviteBlockerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MiscellaneousConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.StarboardConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.alias

open class Service(private val pudding: Pudding) {
    fun PuddingUserProfile.Companion.fromRow(row: ResultRow) = PuddingUserProfile(
        pudding,
        UserProfile(
            UserId(row[Profiles.id].value.toULong()),
            row[Profiles.settings].value,
            row[Profiles.money],
            row[Profiles.isAfk],
            row[Profiles.afkReason]
        )
    )

    fun PuddingProfileSettings.Companion.fromRow(row: ResultRow) = PuddingProfileSettings(
        pudding,
        ProfileSettings(
            row[UserSettings.id].value,
            row[UserSettings.aboutMe],
            row[UserSettings.gender],
            row[UserSettings.activeProfileDesign]?.value,
            row[UserSettings.activeBackground]?.value,
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
            row[ServerConfigs.localeId],
            row[ServerConfigs.starboardConfig]?.value,
            row[ServerConfigs.miscellaneousConfig]?.value,
            row[ServerConfigs.inviteBlockerConfig]?.value
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

    fun PuddingReputation.Companion.fromRow(row: ResultRow) = PuddingReputation(
        pudding,
        Reputation(
            row[Reputations.id].value,
            row[Reputations.givenById],
            row[Reputations.givenByIp],
            row[Reputations.givenByEmail],
            row[Reputations.receivedById],
            row[Reputations.receivedAt],
            row[Reputations.content],
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
            crop,
            row[BackgroundVariations.storageType]
        )
    else
        ProfileDesignGroupBackgroundVariation(
            groupId.toString(),
            row[BackgroundVariations.file],
            row[BackgroundVariations.preferredMediaType],
            crop,
            row[BackgroundVariations.storageType]
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
    } else if (row.getOrNull(DailyRewardSonhosTransactionsLog.id) != null) {
        DailyRewardSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[DailyRewardSonhosTransactionsLog.quantity]
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
    } else if (row.getOrNull(RaffleTicketsSonhosTransactionsLog.id) != null) {
        RaffleTicketsSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[RaffleTicketsSonhosTransactionsLog.sonhos],
            row[RaffleTicketsSonhosTransactionsLog.ticketQuantity]
        )
    } else if (row.getOrNull(RaffleRewardSonhosTransactionsLog.id) != null) {
        RaffleRewardSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[Raffles.alias("r1")[Raffles.paidOutPrize]] ?: -1,
            row[Raffles.alias("r1")[Raffles.paidOutPrizeAfterTax]] ?: row[Raffles.alias("r1")[Raffles.paidOutPrize]] ?: -1,
            row[Raffles.alias("r1")[Raffles.tax]],
            row[Raffles.alias("r1")[Raffles.taxPercentage]],
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
    } else if (row.getOrNull(ShipEffectSonhosTransactionsLog.id) != null) {
        ShipEffectSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[ShipEffectSonhosTransactionsLog.sonhos]
        )
    } else if (row.getOrNull(BotVoteSonhosTransactionsLog.id) != null) {
        BotVoteSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[BotVoteSonhosTransactionsLog.websiteSource],
            row[BotVoteSonhosTransactionsLog.sonhos]
        )
    } else if (row.getOrNull(Christmas2022SonhosTransactionsLog.id) != null) {
        Christmas2022SonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[Christmas2022SonhosTransactionsLog.sonhos],
            row[Christmas2022SonhosTransactionsLog.gifts]
        )
    } else if (row.getOrNull(Easter2023SonhosTransactionsLog.id) != null) {
        Easter2023SonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[Easter2023SonhosTransactionsLog.sonhos],
            row[Easter2023SonhosTransactionsLog.baskets]
        )
    } else {
        UnknownSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
        )
    }
}

fun UserNotification.Companion.fromRow(row: ResultRow): UserNotification {
    // "hasValue" does not work, because it only checks if the value is present on the table BUT it is always present! (but it is null)
    return if (row.getOrNull(DailyTaxWarnUserNotifications.id) != null) {
        DailyTaxWarnUserNotification(
            row[UserNotifications.id].value,
            row[UserNotifications.timestamp].toKotlinInstant(),
            UserId(row[UserNotifications.user].value),
            row[DailyTaxWarnUserNotifications.inactivityTaxTimeWillBeTriggeredAt].toKotlinInstant(),
            row[DailyTaxWarnUserNotifications.currentSonhos],
            row[DailyTaxWarnUserNotifications.howMuchWasRemoved],
            row[DailyTaxWarnUserNotifications.maxDayThreshold],
            row[DailyTaxWarnUserNotifications.minimumSonhosForTrigger],
            row[DailyTaxWarnUserNotifications.tax],
        )
    } else if (row.getOrNull(DailyTaxTaxedUserNotifications.id) != null) {
        DailyTaxTaxedUserNotification(
            row[UserNotifications.id].value,
            row[UserNotifications.timestamp].toKotlinInstant(),
            UserId(row[UserNotifications.user].value),
            row[DailyTaxTaxedUserNotifications.nextInactivityTaxTimeWillBeTriggeredAt].toKotlinInstant(),
            row[DailyTaxTaxedUserNotifications.currentSonhos],
            row[DailyTaxTaxedUserNotifications.howMuchWasRemoved],
            row[DailyTaxTaxedUserNotifications.maxDayThreshold],
            row[DailyTaxTaxedUserNotifications.minimumSonhosForTrigger],
            row[DailyTaxTaxedUserNotifications.tax],
        )
    } else if (row.getOrNull(CorreiosPackageUpdateUserNotifications.id) != null) {
        CorreiosPackageUpdateUserNotification(
            row[UserNotifications.id].value,
            row[UserNotifications.timestamp].toKotlinInstant(),
            UserId(row[UserNotifications.user].value),
            row[TrackedCorreiosPackagesEvents.trackingId],
            Json.parseToJsonElement(row[TrackedCorreiosPackagesEvents.event])
                .jsonObject
        )
    } else {
        UnknownUserNotification(
            row[UserNotifications.id].value,
            row[UserNotifications.timestamp].toKotlinInstant(),
            UserId(row[UserNotifications.user].value),
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

fun StarboardConfig.Companion.fromRow(row: ResultRow) = StarboardConfig(
    row[StarboardConfigs.enabled],
    row[StarboardConfigs.starboardChannelId].toULong(),
    row[StarboardConfigs.requiredStars]
)

fun MiscellaneousConfig.Companion.fromRow(row: ResultRow) = MiscellaneousConfig(
    row[MiscellaneousConfigs.enableBomDiaECia],
    row[MiscellaneousConfigs.enableQuirky],
)

fun InviteBlockerConfig.Companion.fromRow(row: ResultRow) = InviteBlockerConfig(
    row[InviteBlockerConfigs.enabled],
    row[InviteBlockerConfigs.whitelistedChannels].toList(),
    row[InviteBlockerConfigs.whitelistServerInvites],
    row[InviteBlockerConfigs.deleteMessage],
    row[InviteBlockerConfigs.tellUser],
    row[InviteBlockerConfigs.warnMessage],
)

fun ModerationConfig.Companion.fromRow(row: ResultRow) = ModerationConfig(
    row[ModerationConfigs.sendPunishmentViaDm],
    row[ModerationConfigs.sendPunishmentToPunishLog],
    row[ModerationConfigs.punishLogChannelId],
    row[ModerationConfigs.punishLogMessage]
)