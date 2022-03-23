package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Daily

class PuddingDaily(
    private val pudding: Pudding,
    val data: Daily
) {
    companion object;

    val receivedAt by data::receivedAt
}