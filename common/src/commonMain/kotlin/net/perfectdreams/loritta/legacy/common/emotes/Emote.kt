package net.perfectdreams.loritta.legacy.common.emotes

import net.perfectdreams.loritta.legacy.common.entities.Mentionable

abstract class Emote(val code: String) : Mentionable {
    /**
     * Gets the emote name
     */
    abstract fun getName(): String

    override fun toString() = asMention
}