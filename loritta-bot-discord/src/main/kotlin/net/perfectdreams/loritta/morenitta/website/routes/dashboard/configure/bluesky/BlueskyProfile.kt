package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky

import kotlinx.serialization.Serializable

@Serializable
data class BlueskyProfile(
    val did: String,
    val handle: String,
    val displayName: String,
    val avatar: String
) {
    /**
     * Gets the effective name of the user: Their display name or, if it is empty, their handle
     */
    val effectiveName
        get() = displayName.ifEmpty { handle }
}