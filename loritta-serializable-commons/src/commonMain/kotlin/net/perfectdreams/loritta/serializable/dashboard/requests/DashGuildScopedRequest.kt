package net.perfectdreams.loritta.serializable.dashboard.requests

import kotlinx.serialization.Serializable

@Serializable
sealed class DashGuildScopedRequest {
    @Serializable
    data object GetGuildInfoRequest : DashGuildScopedRequest()

    @Serializable
    data object GetGuildWelcomerConfigRequest : DashGuildScopedRequest()
}