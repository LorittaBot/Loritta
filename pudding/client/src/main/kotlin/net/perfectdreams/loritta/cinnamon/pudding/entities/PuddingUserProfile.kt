package net.perfectdreams.loritta.cinnamon.pudding.entities

import kotlinx.datetime.Instant
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserProfile

class PuddingUserProfile(
    private val pudding: Pudding,
    val data: UserProfile
) {
    companion object;

    val id by data::id
    val money by data::money

    /**
     * Gives an achievement to this user
     *
     * @param  type the acheviment type
     * @return if true, the acheviment was successfully given, if false, the user already has the acheviment
     */
    suspend fun giveAchievement(type: AchievementType, achievedAt: Instant): Boolean = pudding.users.giveAchievement(
        id,
        type,
        achievedAt
    )

    suspend fun getRankPositionInSonhosRanking() = pudding.sonhos.getSonhosRankPositionBySonhos(money)
}