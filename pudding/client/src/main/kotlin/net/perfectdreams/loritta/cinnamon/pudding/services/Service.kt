package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Achievement
import net.perfectdreams.loritta.cinnamon.pudding.data.Background
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.DefaultBackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.Marriage
import net.perfectdreams.loritta.cinnamon.pudding.data.ProfileDesignGroupBackgroundVariation
import net.perfectdreams.loritta.cinnamon.pudding.data.Rectangle
import net.perfectdreams.loritta.cinnamon.pudding.data.ServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.data.ShipEffect
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.data.UserProfile
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingBackground
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingShipEffect
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundVariations
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import org.jetbrains.exposed.sql.ResultRow

open class Service(private val pudding: Pudding) {
    fun PuddingUserProfile.Companion.fromRow(row: ResultRow) = PuddingUserProfile(
        pudding,
        UserProfile(
            UserId(row[Profiles.id].value.toULong()),
            row[Profiles.money]
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