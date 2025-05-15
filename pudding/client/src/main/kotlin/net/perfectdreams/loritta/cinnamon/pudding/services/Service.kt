package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.CorreiosPackageUpdateUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxTaxedUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxWarnUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.InviteBlockerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MiscellaneousConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.StarboardConfigs
import net.perfectdreams.loritta.serializable.*
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
            row[Profiles.vacationUntil]?.toKotlinInstant()
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