package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingAchievement
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingProfileSettings
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.common.utils.TokenType
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.UserBannedState
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UsersService(private val pudding: Pudding) : Service(pudding) {
    companion object {
        fun validBannedUsersList(currentMillis: Long) = BannedUsers.select(BannedUsers.userId).where { 
            (BannedUsers.valid eq true) and
                    (
                            BannedUsers.expiresAt.isNull()
                                    or
                                    (
                                            BannedUsers.expiresAt.isNotNull() and
                                                    (BannedUsers.expiresAt greaterEq currentMillis))
                            )

        }

        fun botTokenUsersList() = UserLorittaAPITokens.select(UserLorittaAPITokens.tokenUserId).where {
            UserLorittaAPITokens.tokenType eq TokenType.BOT
        }
    }

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

        return@transaction Profiles.selectAll().where { Profiles.id eq insertId }
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
    suspend fun getUserProfile(id: UserId) = pudding.transaction {
        Profiles.selectFirstOrNull { Profiles.id eq id.value.toLong() }
            ?.let { PuddingUserProfile.fromRow(it) }
    }

    /**
     * Gets multiple [PuddingUserProfile], if the profile doesn't exist, then null is returned
     *
     * @param id the profile's ID
     * @return a list with all user profiles, or null if it doesn't exist
     */
    suspend fun getUserProfiles(id: List<UserId>) = pudding.transaction {
        Profiles.selectAll().where { Profiles.id inList id.map { it.value.toLong() } }
            .associate { it[Profiles.id].value to PuddingUserProfile.fromRow(it) }
    }

    /**
     * Gets multiple [PuddingProfileSettings], if the profile doesn't exist, then null is returned
     *
     * @param id the profile's ID
     * @return a map with all user profiles, or null if it doesn't exist
     */
    suspend fun getProfileSettingsOfUsers(id: List<UserId>) = pudding.transaction {
        Profiles.innerJoin(UserSettings)
            .selectAll().where { Profiles.id inList id.map { it.value.toLong() } }
            .associate { it[Profiles.id].value to PuddingProfileSettings.fromRow(it) }
    }

    /**
     * Gets a [PuddingProfileSettings], if the profile doesn't exist, then null is returned
     *
     * @param id the user's profile settings ID
     * @return the user settings or null if it doesn't exist
     */
    suspend fun getProfileSettings(id: Long): PuddingProfileSettings? {
        return pudding.transaction {
            UserSettings.selectFirstOrNull { UserSettings.id eq id }
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
            val alreadyHasAchievement = UserAchievements.selectFirstOrNull {
                UserAchievements.user eq id.value.toLong() and (UserAchievements.type eq type)
            } != null

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
            UserAchievements.selectAll().where {
                UserAchievements.user eq id.value.toLong() and (UserAchievements.type inList AchievementType.entries.filter { it.available })
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
        val bannedState = BannedUsers.selectAll().where {
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
                it[BannedUsers.bannedBy]?.let { UserId(it.toULong()) },
                it[BannedUsers.staffNotes]
            )
        }
    }

    /**
     * Gets [userId]'s cached user info from the database if it is present
     *
     * @param userId the user's ID
     * @return the cached user info or null if it doesn't exist
     */
    suspend fun getCachedUserInfoById(userId: UserId) = pudding.transaction {
        val info = CachedDiscordUsers.selectFirstOrNull {
            CachedDiscordUsers.id eq userId.value.toLong()
        } ?: return@transaction null

        CachedUserInfo(
            UserId(info[CachedDiscordUsers.id].value),
            info[CachedDiscordUsers.name],
            info[CachedDiscordUsers.discriminator],
            info[CachedDiscordUsers.globalName],
            info[CachedDiscordUsers.avatarId]
        )
    }

    /**
     * Gets [name]'s cached user info from the database if it is present (Pomelo)
     *
     * @param userId the user's ID
     * @return the cached user info or null if it doesn't exist
     */
    suspend fun getCachedUserInfoByPomeloName(name: String) = pudding.transaction {
        val info = CachedDiscordUsers.selectFirstOrNull {
            CachedDiscordUsers.name eq name and (CachedDiscordUsers.discriminator eq "0000")
        } ?: return@transaction null

        CachedUserInfo(
            UserId(info[CachedDiscordUsers.id].value),
            info[CachedDiscordUsers.name],
            info[CachedDiscordUsers.discriminator],
            info[CachedDiscordUsers.globalName],
            info[CachedDiscordUsers.avatarId]
        )
    }

    /**
     * Gets [name]#[discriminator]'s cached user info from the database if it is present
     *
     * @param userId the user's ID
     * @return the cached user info or null if it doesn't exist
     */
    suspend fun getCachedUserInfoByNameAndDiscriminator(name: String, discriminator: String) = pudding.transaction {
        val info = CachedDiscordUsers.selectFirstOrNull {
            CachedDiscordUsers.name eq name and (CachedDiscordUsers.discriminator eq discriminator)
        } ?: return@transaction null

        CachedUserInfo(
            UserId(info[CachedDiscordUsers.id].value),
            info[CachedDiscordUsers.name],
            info[CachedDiscordUsers.discriminator],
            info[CachedDiscordUsers.globalName],
            info[CachedDiscordUsers.avatarId]
        )
    }

    /**
     * Inserts or updates [userId]'s cached user info
     *
     * The cached user info can be retrieved with [getCachedUserInfoById], avoiding Discord API calls just to pull user information
     *
     * @param userId        the user's ID
     * @param name          the user's name
     * @param discriminator the user's discriminator
     * @param avatarId      the user's avatar ID
     */
    suspend fun insertOrUpdateCachedUserInfo(
        userId: UserId,
        name: String,
        discriminator: String,
        globalName: String?,
        avatarId: String?
    ) = pudding.transaction {
        val info = CachedDiscordUsers.selectFirstOrNull {
            CachedDiscordUsers.id eq userId.value.toLong()
        }

        val now = System.currentTimeMillis()
        if (info != null) {
            CachedDiscordUsers.update({ CachedDiscordUsers.id eq userId.value.toLong() }) {
                it[CachedDiscordUsers.name] = name
                it[CachedDiscordUsers.discriminator] = discriminator
                it[CachedDiscordUsers.globalName] = globalName
                it[CachedDiscordUsers.avatarId] = avatarId
                it[CachedDiscordUsers.updatedAt] = now
            }
        } else {
            CachedDiscordUsers.insert {
                it[CachedDiscordUsers.id] = userId.value.toLong()
                it[CachedDiscordUsers.name] = name
                it[CachedDiscordUsers.discriminator] = discriminator
                it[CachedDiscordUsers.avatarId] = avatarId
                it[CachedDiscordUsers.createdAt] = now
                it[CachedDiscordUsers.updatedAt] = now
            }
        }
    }

    suspend fun getCachedDiscordDirectMessageChannel(id: UserId): Long? {
        return pudding.transaction {
            CachedDiscordUsersDirectMessageChannels.selectFirstOrNull { CachedDiscordUsersDirectMessageChannels.id eq id.value.toLong() }
        }?.get(CachedDiscordUsersDirectMessageChannels.channelId)
    }

    suspend fun insertOrUpdateCachedDiscordDirectMessageChannel(id: UserId, channelId: Long) {
        return pudding.transaction {
            val info = CachedDiscordUsersDirectMessageChannels.selectFirstOrNull {
                CachedDiscordUsersDirectMessageChannels.id eq id.value.toLong()
            }

            if (info != null) {
                CachedDiscordUsersDirectMessageChannels.update({ CachedDiscordUsersDirectMessageChannels.id eq id.value.toLong() }) {
                    it[CachedDiscordUsersDirectMessageChannels.channelId] = channelId
                }
            } else {
                CachedDiscordUsersDirectMessageChannels.insert {
                    it[CachedDiscordUsersDirectMessageChannels.id] = id.value.toLong()
                    it[CachedDiscordUsersDirectMessageChannels.channelId] = channelId
                }
            }
        }
    }

    suspend fun deleteCachedDiscordDirectMessageChannel(id: UserId) {
        return pudding.transaction {
            CachedDiscordUsersDirectMessageChannels.deleteWhere {
                CachedDiscordUsersDirectMessageChannels.id eq id.value.toLong()
            }
        }
    }
}