package net.perfectdreams.loritta.profile

import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Marriage
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.utils.CachedUserInfo
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

object ProfileUtils {
    /**
     * Gets the marriage information of the [userProfile]
     *
     * If the profile does not have an associated marriage, this will return null.
     *
     * @param  userProfile the user's profile
     * @return the marriage information or null if the user isn't married
     */
    suspend fun getMarriageInfo(userProfile: Profile): MarriageInfo? {
        val marriage = loritta.newSuspendedTransaction { userProfile.marriage }

        if (marriage != null) {
            val marriedWithId = if (marriage.user1 == userProfile.id.value) {
                marriage.user2
            } else {
                marriage.user1
            }.toString()

            return lorittaShards.retrieveUserInfoById(marriedWithId.toLong())?.let {
                MarriageInfo(marriage, it)
            }
        }

        return null
    }

    /**
     * Gets how many reputations the [userInfo] has
     *
     * @param  userInfo the user's information
     * @return the reputation count
     */
    suspend fun getReputationCount(userInfo: ProfileUserInfoData) = loritta.newSuspendedTransaction {
        Reputations.select { Reputations.receivedById eq userInfo.id }.count()
    }

    /**
     * Gets the user's global position in the experience ranking
     *
     * @param  userProfile the user's profile
     * @return the user's current global position in the experience ranking
     */
    suspend fun getGlobalExperiencePosition(userProfile: Profile) =
            // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any XP, we just return null! :3
            if (userProfile.xp >= 100_000L)
                loritta.newSuspendedTransaction {
                    Profiles.select { Profiles.xp greaterEq userProfile.xp }.count()
                } else null

    /**
     * Gets the user's global position in the economy ranking
     *
     * @param  userProfile the user's profile
     * @return the user's current global position in the economy ranking
     */
    suspend fun getGlobalEconomyPosition(userProfile: Profile) =
            // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any sonhos, we just return null! :3
            if (userProfile.money >= 100_000L)
                loritta.newSuspendedTransaction {
                    Profiles.select { Profiles.money greaterEq userProfile.money }.count()
                } else null

    /**
     * Gets the user's local profile in the [guild] or null if the user does not have a profile in the guild
     *
     * @param  guild    the guild that the profile will be retrieved from
     * @param  userInfo the user's information
     * @return the user's current local position in the experience ranking
     */
    suspend fun getLocalProfile(guild: Guild, userInfo: ProfileUserInfoData) = loritta.newSuspendedTransaction {
        GuildProfile.find { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.userId eq userInfo.id) }.firstOrNull()
    }

    /**
     * Gets the user's local position in the experience ranking
     *
     * @param  localProfile the user's local profile
     * @return the user's current local position in the experience ranking
     */
    suspend fun getLocalExperiencePosition(localProfile: GuildProfile?) = if (localProfile != null && localProfile.xp != 0L) {
        // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any local XP, we just return null! :3
        loritta.newSuspendedTransaction {
            GuildProfiles.select { (GuildProfiles.guildId eq localProfile.guildId) and (GuildProfiles.xp greaterEq localProfile.xp) }.count()
        }
    } else {
        null
    }

    data class MarriageInfo(
            val marriage: Marriage,
            val partner: CachedUserInfo
    )
}