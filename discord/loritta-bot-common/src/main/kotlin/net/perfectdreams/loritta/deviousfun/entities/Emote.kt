package net.perfectdreams.loritta.deviousfun.entities

import net.perfectdreams.loritta.deviousfun.DeviousFun

interface Emote {
    val deviousFun: DeviousFun
    val name: String
    val asMention: String
}