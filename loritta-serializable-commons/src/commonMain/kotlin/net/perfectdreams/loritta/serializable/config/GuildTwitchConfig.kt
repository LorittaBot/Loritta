package net.perfectdreams.loritta.serializable.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.TwitchUser

@Serializable
data class GuildTwitchConfig(
    val trackedTwitchAccounts: List<TrackedTwitchAccountWithTwitchUserAndTrackingState>,
    val premiumTrackTwitchAccounts: List<PremiumTrackTwitchAccountWithTwitchUser>,
) {
    @Serializable
    data class TrackedTwitchAccountWithTwitchUserAndTrackingState(
        val trackingState: TwitchAccountTrackState,
        val trackedInfo: TrackedTwitchAccount,
        val twitchUser: TwitchUser?
    )

    @Serializable
    data class PremiumTrackTwitchAccountWithTwitchUser(
        val trackedInfo: PremiumTrackTwitchAccount,
        val twitchUser: TwitchUser?
    )
}