package net.perfectdreams.loritta.api.entities

abstract class LorittaEmote(val code: String) : Mentionable {
    /**
     * Gets the emote name
     */
    abstract fun getName(): String

    /**
     * If the emote is available for use
     *
     * @return if the emote is available
     */
    open fun isAvailable(): Boolean {
        return true
    }
}