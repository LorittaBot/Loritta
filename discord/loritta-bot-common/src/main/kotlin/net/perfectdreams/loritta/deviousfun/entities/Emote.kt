package net.perfectdreams.loritta.deviousfun.entities

import net.perfectdreams.loritta.deviousfun.JDA

interface Emote {
    val jda: JDA
    val name: String
    val asMention: String
}