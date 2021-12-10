package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Reputation

class PuddingReputation(
    private val pudding: Pudding,
    val data: Reputation
) {
    companion object;

    val givenById by data::givenById
    val receivedAt by data::receivedAt
    val content by data::content
}