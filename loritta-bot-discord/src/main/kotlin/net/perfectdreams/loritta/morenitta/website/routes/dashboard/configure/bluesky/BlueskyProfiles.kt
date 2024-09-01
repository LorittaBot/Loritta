package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky

import kotlinx.serialization.Serializable

@Serializable
data class BlueskyProfiles(
    val profiles: List<BlueskyProfile>
)