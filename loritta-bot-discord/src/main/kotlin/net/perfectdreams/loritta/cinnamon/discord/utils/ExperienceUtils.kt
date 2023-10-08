package net.perfectdreams.loritta.cinnamon.discord.utils

object ExperienceUtils {
    fun getLevelExperience(lvl: Int): Long {
        return lvl * 1000L
    }

    fun getHowMuchExperienceIsLeftToLevelUp(currentExperience: Long, lvl: Int): Long {
        return getLevelExperience(lvl) - currentExperience
    }

    fun getCurrentLevelForXp(xp: Long) = (xp / 1000).toInt()
}