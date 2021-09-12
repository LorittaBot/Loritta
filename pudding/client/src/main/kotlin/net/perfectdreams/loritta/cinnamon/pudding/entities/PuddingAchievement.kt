package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Achievement

class PuddingAchievement(
    private val pudding: Pudding,
    val data: Achievement
) {
    companion object;

    val user by data::user
    val type by data::type
    val achievedAt by data::achievedAt
}