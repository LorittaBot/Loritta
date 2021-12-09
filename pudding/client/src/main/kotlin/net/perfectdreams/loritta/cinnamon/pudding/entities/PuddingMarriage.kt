package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Marriage

class PuddingMarriage(
    private val pudding: Pudding,
    val data: Marriage
) {
    companion object;

    val id by data::id
    val user1 by data::user1
    val user2 by data::user2
    val marriedSince by data::marriedSince
}