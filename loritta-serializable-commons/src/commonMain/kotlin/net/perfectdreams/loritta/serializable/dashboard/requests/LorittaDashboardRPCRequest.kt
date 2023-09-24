package net.perfectdreams.loritta.serializable.dashboard.requests

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
sealed class LorittaDashboardRPCRequest {
    @Serializable
    class ExecuteDashGuildScopedRPCRequest(
        val guildId: Long,
        val request: DashGuildScopedRequest
    ) : LorittaDashboardRPCRequest()

    @Serializable
    class GetUserGuildsRequest : LorittaDashboardRPCRequest()

    @Serializable
    class PutPowerStreamClaimedLimitedTimeSonhosRewardRequest(
        val userId: Long,
        val quantity: Long,
        val liveId: String,
        val streamId: Long,
        val rewardId: Long
    ) : LorittaDashboardRPCRequest()

    @Serializable
    class PutPowerStreamClaimedFirstSonhosRewardRequest(
        val userId: Long,
        val quantity: Long,
        val liveId: String,
        val streamId: Long
    ) : LorittaDashboardRPCRequest()

    @Serializable
    class UpdateLorittaActivityRequest(
        val text: String,
        val activityType: String,
        val priority: Int,
        val startsAt: Instant,
        val endsAt: Instant,
        val streamUrl: String?
    ) : LorittaDashboardRPCRequest()

    @Serializable
    class GetSpicyInfoRequest : LorittaDashboardRPCRequest()
}