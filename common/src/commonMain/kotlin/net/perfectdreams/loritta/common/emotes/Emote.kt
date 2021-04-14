package net.perfectdreams.loritta.common.emotes

import net.perfectdreams.loritta.common.entities.Mentionable

abstract class Emote(val code: String) : Mentionable {
    /**
     * Gets the emote name
     */
    abstract fun getName(): String

    override fun toString() = asMention
}