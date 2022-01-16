package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Achievement
import net.perfectdreams.loritta.cinnamon.pudding.data.Background
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.DefaultBackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.Marriage
import net.perfectdreams.loritta.cinnamon.pudding.data.ProfileDesignGroupBackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.ProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.data.Rectangle
import net.perfectdreams.loritta.cinnamon.pudding.data.ServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.data.ShipEffect
import net.perfectdreams.loritta.cinnamon.pudding.data.SonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.SparklyPowerLSXSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.data.UserProfile
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingShipEffect
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundVariations
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.BrokerSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
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
            row[Profiles.money],
            row[Profiles.isAfk],
            row[Profiles.afkReason]
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
    return if (row.getOrNull(BrokerSonhosTransactionsLog.id) != null) {
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
    } else if (row.getOrNull(SparklyPowerLSXSonhosTransactionsLog.id) != null) {
        SparklyPowerLSXSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
            row[SparklyPowerLSXSonhosTransactionsLog.action],
            row[SparklyPowerLSXSonhosTransactionsLog.sonhos],
            row[SparklyPowerLSXSonhosTransactionsLog.afterExchangeRateSonhos],
            row[SparklyPowerLSXSonhosTransactionsLog.playerName],
            row[SparklyPowerLSXSonhosTransactionsLog.playerUniqueId].toString(),
            row[SparklyPowerLSXSonhosTransactionsLog.exchangeRate]
        )
    } else {
        UnknownSonhosTransaction(
            row[SonhosTransactionsLog.id].value,
            row[SonhosTransactionsLog.timestamp].toKotlinInstant(),
            UserId(row[SonhosTransactionsLog.user].value),
        )
    }
}
