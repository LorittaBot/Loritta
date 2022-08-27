package net.perfectdreams.loritta.cinnamon.discord.utils.profiles

import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

abstract class ProfileCreator(val loritta: LorittaCinnamon, val internalName: String) {
    /**
     * Gets the user's global position in the economy ranking
     *
     * @param  userProfile the user's profile
     * @return the user's current global position in the economy ranking
     */
    suspend fun getGlobalEconomyPosition(userProfile: PuddingUserProfile) =
        // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any sonhos, we just return null! :3
        if (userProfile.money >= 100_000L)
            loritta.services.transaction {
                Profiles.select { Profiles.money greaterEq userProfile.money }.count()
            } else null

    /**
     * Gets the user's local position in the experience ranking
     *
     * @param  localProfile the user's local profile
     * @return the user's current local position in the experience ranking
     */
    suspend fun getLocalExperiencePosition(localProfile: ResultRow?) = if (localProfile != null && localProfile[GuildProfiles.xp] != 0L) {
        // This is a optimization: Querying the user's position if he has 0 takes too long, if the user does *not* have any local XP, we just return null! :3
        loritta.services.transaction {
            GuildProfiles.select { (GuildProfiles.guildId eq localProfile[GuildProfiles.guildId]) and (GuildProfiles.xp greaterEq localProfile[GuildProfiles.xp]) }.count()
        }
    } else {
        null
    }
}