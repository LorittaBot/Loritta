package net.perfectdreams.loritta.common.entities

/**
 * Used for Discord Interaction-like message channels, with support for message deferring
 */
interface InteractionMessageChannel : MessageChannel {
    /**
     * Defers the application command request message
     *
     * @param isEphemeral if the deferred message should be ephemeral or not
     */
    suspend fun deferMessage(isEphemeral: Boolean)
}