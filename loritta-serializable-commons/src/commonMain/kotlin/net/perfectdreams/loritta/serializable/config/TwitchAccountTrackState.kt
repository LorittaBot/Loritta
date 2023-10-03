package net.perfectdreams.loritta.serializable.config

enum class TwitchAccountTrackState {
    /**
     * Twitch account is being tracked because it has been authorized by the owner
     */
    AUTHORIZED,

    /**
     * Twitch account is being tracked because it is in the "always track" list
     */
    ALWAYS_TRACK_USER,

    /**
     * Twitch account is being tracked because another premium user is tracking this account
     */
    PREMIUM_TRACK_USER,

    /**
     * Twitch account is not being tracked because it isn't authorized
     */
    UNAUTHORIZED
}