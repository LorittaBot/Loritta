package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.Marriage

class PuddingMarriage(
    private val pudding: Pudding,
    val data: Marriage
) {
    companion object;

    val id by data::id
    // These are temporary, this WILL need to be changed after Loritta supports more than 2 participants on the marriage
    val user1 = data.participants[0]
    val user2 = data.participants[1]
    val participants by data::participants
    val marriedSince by data::marriedSince
}