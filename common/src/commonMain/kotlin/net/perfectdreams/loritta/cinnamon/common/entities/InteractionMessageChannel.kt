package net.perfectdreams.loritta.cinnamon.common.entities

/**
 * Used for Discord Interaction-like message channels, with support for message deferring
 */
interface InteractionMessageChannel : MessageChannel {
    /**
     * Defers the application command request message
     */
    suspend fun deferChannelMessage()

    /**
     * Defers the application command request message
     */
    suspend fun deferChannelMessageEphemerally()
}