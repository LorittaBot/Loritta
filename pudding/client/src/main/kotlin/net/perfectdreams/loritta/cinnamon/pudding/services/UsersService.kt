package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserBannedState
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class UsersService(private val pudding: Pudding) : Service(pudding) {
    /**
     * Gets or creates a [PuddingUserProfile]
     *
     * @param  id the profile's ID
     * @return the user profile
     */
    suspend fun getOrCreateUserProfile(id: UserId) = pudding.transaction {
        val profile = getUserProfile(id)
        if (profile != null)
            return@transaction profile

        val profileSettings = UserSettings.insert {
            it[gender] = Gender.UNKNOWN
        }

        val insertId = Profiles.insertAndGetId {
            it[Profiles.id] = id.value.toLong()
            it[Profiles.xp] = 0
            it[Profiles.lastMessageSentAt] = 0L
            it[Profiles.lastMessageSentHash] = 0
            it[Profiles.money] = 0
            it[Profiles.isAfk] = false
            it[Profiles.afkReason] = null
            it[Profiles.settings] = profileSettings[UserSettings.id]
        }

        return@transaction Profiles.select { Profiles.id eq insertId }
            .limit(1)
            .first() // Should NEVER be null!
            .let {
                PuddingUserProfile
                    .fromRow(it)
            }
    }

    /**
     * Gets a [PuddingUserProfile], if the profile doesn't exist, then null is returned
     *
     * @param id the profile's ID
     * @return the user profile or null if it doesn't exist
     */
    suspend fun getUserProfile(id: UserId): PuddingUserProfile? {
        return pudding.transaction {
            Profiles.select { Profiles.id eq id.value.toLong() }
                .firstOrNull()
        }?.let { PuddingUserProfile.fromRow(it) }
    }

    /**
     * Gets or creates a [PuddingProfileSettings]
     *
     * @param  id the user's ID
     * @return the user settings
     */
    suspend fun getOrCreateProfileSettings(id: UserId) = pudding.transaction {
        val setting = getProfileSettings(id)
        if (setting != null)
            return@transaction setting

        val insertId = UserSettings.insertAndGetId {
            it[UserSettings.id] = id.value.toLong()
            it[UserSettings.aboutMe] = null
            it[UserSettings.gender] = Gender.UNKNOWN
            it[UserSettings.activeProfileDesign] = null
            it[UserSettings.activeBackground] = null
            it[UserSettings.doNotSendXpNotificationsInDm] = false
            it[UserSettings.discordAccountFlags] = 0
            it[UserSettings.discordPremiumType] = null
            it[UserSettings.language] = null
        }

        return@transaction UserSettings.select { UserSettings.id eq insertId }
            .limit(1)
            .first() // Should NEVER be null!
            .let {
                PuddingProfileSettings
                    .fromRow(it)
            }
    }

    /**
     * Gets a [PuddingProfileSettings], if the profile doesn't exist, then null is returned
     *
     * @param id the user's ID
     * @return the user settings or null if it doesn't exist
     */
    suspend fun getProfileSettings(id: UserId): PuddingProfileSettings? {
        return pudding.transaction {
            UserSettings.select { UserSettings.id eq id.value.toLong() }
                .firstOrNull()
        }?.let { PuddingProfileSettings.fromRow(it) }
    }

    /**
     * Gives an achievement to the user
     *
     * @param  id the profile's ID
     * @param  type the achievement type
     * @return if true, the achievement was successfully given, if false, the user already has the achievement
     */
    suspend fun giveAchievement(id: UserId, type: AchievementType, achievedAt: Instant): Boolean {
        return pudding.transaction {
            val alreadyHasAchievement = UserAchievements.select {
                UserAchievements.user eq id.value.toLong() and (UserAchievements.type eq type)
            }.firstOrNull() != null

            if (alreadyHasAchievement)
                return@transaction false

            UserAchievements.insert {
                it[UserAchievements.user] = id.value.toLong()
                it[UserAchievements.type] = type
                it[UserAchievements.achievedAt] = achievedAt.toJavaInstant()
            }

            return@transaction true
        }
    }

    /**
     * Gets achievements of a user
     *
     * @param  id the profile's ID
     * @return the achievement list
     */
    suspend fun getUserAchievements(id: UserId): List<PuddingAchievement> {
        return pudding.transaction {
            UserAchievements.select {
                UserAchievements.user eq id.value.toLong()
            }.map { PuddingAchievement.fromRow(it) }
        }
    }

    /**
     * Get the user's current banned state, if it exists and if it is valid
     *
     * @param id the user's ID
     * @return the user banned state or null if it doesn't exist
     */
    suspend fun getUserBannedState(id: UserId) = pudding.transaction {
        val bannedState = BannedUsers.select {
            BannedUsers.userId eq id.value.toLong() and
                    (BannedUsers.valid eq true) and
                    (
                            BannedUsers.expiresAt.isNull()
                                    or
                                    (
                                            BannedUsers.expiresAt.isNotNull() and
                                                    (BannedUsers.expiresAt greaterEq System.currentTimeMillis()))
                            )

        }
            .orderBy(BannedUsers.bannedAt, SortOrder.DESC)
            .firstOrNull() ?: return@transaction null

        return@transaction bannedState.let {
            UserBannedState(
                it[BannedUsers.valid],
                Instant.fromEpochMilliseconds(it[BannedUsers.bannedAt]),
                it[BannedUsers.expiresAt]?.let { Instant.fromEpochMilliseconds(it) },
                it[BannedUsers.reason],
                it[BannedUsers.bannedBy]?.let { UserId(it.toULong()) }
            )
        }
    }

    suspend fun setAboutMe(userId: Long, text: String) = pudding.transaction {
        UserSettings.update({ UserSettings.id eq userId }) {
            it[UserSettings.aboutMe] = text
        }
    }
}