package net.perfectdreams.loritta.deviousfun.entities

import net.perfectdreams.loritta.deviousfun.DeviousShard

interface Emote {
    val deviousShard: DeviousShard
    val name: String
    val asMention: String
}