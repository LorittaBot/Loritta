package net.perfectdreams.loritta.utils

object ExperienceUtils {
    fun getLevelExperience(lvl: Int): Long {
        return lvl * 1000L
    }

    fun getHowMuchExperienceIsLeftToLevelUp(currentExperience: Long, lvl: Int): Long {
        return getLevelExperience(lvl) - currentExperience
    }
}