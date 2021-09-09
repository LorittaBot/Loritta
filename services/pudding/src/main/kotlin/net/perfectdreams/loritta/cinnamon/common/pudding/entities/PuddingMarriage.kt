package net.perfectdreams.loritta.cinnamon.common.pudding.entities

import net.perfectdreams.loritta.cinnamon.common.entities.Marriage

class PuddingMarriage(val marriage: net.perfectdreams.loritta.pudding.common.data.Marriage) : Marriage {
    override val id by marriage::id
    override val user1 by marriage::user1
    override val user2 by marriage::user2
    override val marriedSince by marriage::marriedSince
}