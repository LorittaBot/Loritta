package net.perfectdreams.loritta.cinnamon.pudding.entities

import kotlinx.datetime.Instant
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.UserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.sql.update

class PuddingUserProfile(
    private val pudding: Pudding,
    val data: UserProfile
) {
    companion object;

    val id by data::id
    val profileSettingsId by data::profileSettingsId
    val money by data::money
    val isAfk by data::isAfk
    val afkReason by data::afkReason
    val vacationUntil by data::vacationUntil

    /**
     * Gives an achievement to this user
     *
     * @param  type the achievement type
     * @return if true, the achievement was successfully given, if false, the user already has the achievement
     */
    suspend fun giveAchievement(type: AchievementType, achievedAt: Instant): Boolean = pudding.users.giveAchievement(
        id,
        type,
        achievedAt
    )

    suspend fun getRankPositionInSonhosRanking() = pudding.sonhos.getSonhosRankPositionBySonhos(money)

    suspend fun enableAfk(reason: String? = null) {
        if (!isAfk || this.afkReason != reason) {
            pudding.transaction {
                Profiles.update({ Profiles.id eq this@PuddingUserProfile.id.value.toLong() }) {
                    it[isAfk] = true
                    it[afkReason] = reason
                }
            }
        }
    }


    suspend fun disableAfk() {
        if (isAfk || this.afkReason != null) {
            pudding.transaction {
                Profiles.update({ Profiles.id eq this@PuddingUserProfile.id.value.toLong() }) {
                    it[isAfk] = false
                    it[afkReason] = null
                }
            }
        }
    }

    // Should ALWAYS not be null
    suspend fun getProfileSettings() = pudding.users.getProfileSettings(profileSettingsId)!!

    /**
     * Get the user's current banned state, if it exists and if it is valid
     *
     * @return the user banned state or null if it doesn't exist
     */
    suspend fun getBannedState() = pudding.users.getUserBannedState(id)
}