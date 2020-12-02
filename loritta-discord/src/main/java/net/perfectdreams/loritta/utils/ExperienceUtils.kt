package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.Member
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

object ExperienceUtils {
    fun getLevelExperience(lvl: Int): Long {
        return lvl * 1000L
    }

    fun getHowMuchExperienceIsLeftToLevelUp(currentExperience: Long, lvl: Int): Long {
        return getLevelExperience(lvl) - currentExperience
    }

    suspend fun getExperienceCustomTokens(config: ServerConfig, member: Member): Map<String, String> {
        val customTokens = mutableMapOf<String, String>()

        // Load tokens for experience/level/xp
        val profile = config.getUserData(member.idLong)
        val level = profile.getCurrentLevel().currentLevel
        val xp = profile.xp

        val currentLevelTotalXp = getLevelExperience(level)

        val nextLevel = level + 1
        val nextLevelTotalXp = getLevelExperience(nextLevel)
        val nextLevelRequiredXp = getHowMuchExperienceIsLeftToLevelUp(profile.xp, nextLevel)

        val ranking = loritta.newSuspendedTransaction {
            GuildProfiles.select {
                GuildProfiles.guildId eq member.guild.idLong and
                        (GuildProfiles.xp greaterEq profile.xp)
            }.count()
        }

        customTokens[Placeholders.EXPERIENCE_LEVEL_SHORT.name] = level.toString()
        customTokens[Placeholders.EXPERIENCE_XP_SHORT.name] = xp.toString()

        customTokens[Placeholders.EXPERIENCE_NEXT_LEVEL.name] = nextLevel.toString()
        customTokens[Placeholders.EXPERIENCE_NEXT_LEVEL_TOTAL_XP.name] = nextLevelTotalXp.toString()
        customTokens[Placeholders.EXPERIENCE_NEXT_LEVEL_REQUIRED_XP.name] = nextLevelRequiredXp.toString()
        customTokens[Placeholders.EXPERIENCE_RANKING.name] = ranking.toString()
        return customTokens
    }
}