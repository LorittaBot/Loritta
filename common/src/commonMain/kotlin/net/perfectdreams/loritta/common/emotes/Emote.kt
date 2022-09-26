package net.perfectdreams.loritta.common.emotes

import net.perfectdreams.loritta.common.entities.Mentionable

sealed class Emote : Mentionable {
    /**
     * The emote name
     */
    abstract val name: String

    override fun toString() = asMention
}