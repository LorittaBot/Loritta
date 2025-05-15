package net.perfectdreams.loritta.morenitta.profile

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.GuildProfile
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.CachedUserInfo
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

object ProfileUtils {
    /**
     * Gets the marriage information of the [userProfile]
     *
     * If the profile does not have an associated marriage, this will return null.
     *
     * @param  userProfile the user's profile
     * @return the marriage information or null if the user isn't married
     */
    suspend fun getMarriageInfo(loritta: LorittaBot, userProfile: Profile): MarriageInfo? {
        val marriage = loritta.pudding.marriages.getMarriageByUser(UserId(userProfile.userId)) ?: return null

        val partnerId = if (marriage.user1.value.toLong() == userProfile.id.value) {
            marriage.user2
        } else {
            marriage.user1
        }

        KotlinLogging.logger {}.info { "ProfileUtils#retrieveUserInfoById - UserId: ${partnerId}" }
        val partner = loritta.lorittaShards.retrieveUserInfoById(partnerId) ?: return null

        return MarriageInfo(
            marriage,
            partner
        )
    }

    /**
     * Gets how many reputations the [userInfo] has
     *
     * @param  userInfo the user's information
     * @return the reputation count
     */
    suspend fun getReputationCount(loritta: LorittaBot, userInfo: ProfileUserInfoData) = loritta.newSuspendedTransaction {
        Reputations.selectAll().where { Reputations.receivedById eq userInfo.id.toLong() }.count()
    }

    /**
     * Gets the user's global position in the economy ranking
     *
     * @param  userProfile the user's profile
     * @return the user's current global position in the economy ranking
     */
    suspend fun getGlobalEconomyPosition(loritta: LorittaBot, userProfile: Profile) =
        // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any sonhos, we just return null! :3
        if (userProfile.money >= 100_000L)
            loritta.newSuspendedTransaction {
                Profiles.selectAll().where { Profiles.money greaterEq userProfile.money }.count()
            } else null

    /**
     * Gets the user's local profile in the [guild] or null if the user does not have a profile in the guild
     *
     * @param  guild    the guild that the profile will be retrieved from
     * @param  userInfo the user's information
     * @return the user's current local position in the experience ranking
     */
    suspend fun getLocalProfile(loritta: LorittaBot, guild: ProfileGuildInfoData, userInfo: ProfileUserInfoData) = loritta.newSuspendedTransaction {
        GuildProfile.find { (GuildProfiles.guildId eq guild.id.toLong()) and (GuildProfiles.userId eq userInfo.id.toLong()) }.firstOrNull()
    }

    /**
     * Gets the user's local position in the experience ranking
     *
     * @param  localProfile the user's local profile
     * @return the user's current local position in the experience ranking
     */
    suspend fun getLocalExperiencePosition(loritta: LorittaBot, localProfile: GuildProfile?) = if (localProfile != null && localProfile.xp != 0L) {
        // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any local XP, we just return null! :3
        loritta.newSuspendedTransaction {
            GuildProfiles.selectAll().where { (GuildProfiles.guildId eq localProfile.guildId) and (GuildProfiles.xp greaterEq localProfile.xp) }.count()
        }
    } else {
        null
    }

    data class MarriageInfo(
        val marriage: PuddingMarriage,
        val partner: CachedUserInfo
    )
}