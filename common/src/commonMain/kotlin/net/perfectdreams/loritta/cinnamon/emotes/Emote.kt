package net.perfectdreams.loritta.cinnamon.emotes

import net.perfectdreams.loritta.cinnamon.entities.Mentionable

sealed class Emote : net.perfectdreams.loritta.cinnamon.entities.Mentionable {
    /**
     * The emote name
     */
    abstract val name: String

    override fun toString() = asMention
}