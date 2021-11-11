package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

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
}