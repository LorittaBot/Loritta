package net.perfectdreams.loritta.cinnamon.common.emotes

import net.perfectdreams.loritta.cinnamon.common.entities.Mentionable

sealed class Emote : Mentionable {
    /**
     * The emote name
     */
    abstract val name: String

    override fun toString() = asMention
}