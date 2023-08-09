package net.perfectdreams.loritta.serializable.dashboard.requests

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig

@Serializable
sealed class LorittaDashboardRPCRequest {
    @Serializable
    class GetGuildInfoRequest(val guildId: Long) : LorittaDashboardRPCRequest()

    @Serializable
    class GetUserGuildsRequest : LorittaDashboardRPCRequest()

    @Serializable
    class GetGuildGamerSaferConfigRequest(val guildId: Long) : LorittaDashboardRPCRequest()

    @Serializable
    class UpdateGuildGamerSaferConfigRequest(val guildId: Long, val config: GuildGamerSaferConfig) : LorittaDashboardRPCRequest()

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
}