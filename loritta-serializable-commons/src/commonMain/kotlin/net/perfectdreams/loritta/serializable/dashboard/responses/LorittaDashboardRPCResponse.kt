package net.perfectdreams.loritta.serializable.dashboard.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.EnvironmentType
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.PublicLorittaCluster

@Serializable
sealed class LorittaDashboardRPCResponse {
    interface UnknownGuildError
    interface UnknownMemberError
    interface MissingPermissionError

    @Serializable
    class ExecuteDashGuildScopedRPCResponse(val dashResponse: DashGuildScopedResponse) : LorittaDashboardRPCResponse()

    @Serializable
    sealed class GetUserGuildsResponse : LorittaDashboardRPCResponse() {
        @Serializable
        class Success(val guilds: List<DiscordGuild>) : GetUserGuildsResponse()

        @Serializable
        class InvalidDiscordAuthorization : GetUserGuildsResponse()

        @Serializable
        data class DiscordGuild(
            val id: Long,
            val name: String,
            val icon: String?,
            val owner: Boolean,
            val permissions: Long,
            val features: List<String>
        )
    }

    @Serializable
    sealed class PutPowerStreamClaimedLimitedTimeSonhosRewardResponse : LorittaDashboardRPCResponse() {
        @Serializable
        class Success : PutPowerStreamClaimedLimitedTimeSonhosRewardResponse()

        @Serializable
        class UnknownUser : PutPowerStreamClaimedLimitedTimeSonhosRewardResponse()

        @Serializable
        class Unauthorized : PutPowerStreamClaimedLimitedTimeSonhosRewardResponse()
    }

    @Serializable
    sealed class PutPowerStreamClaimedFirstSonhosRewardResponse : LorittaDashboardRPCResponse() {
        @Serializable
        class Success : PutPowerStreamClaimedFirstSonhosRewardResponse()

        @Serializable
        class UnknownUser : PutPowerStreamClaimedFirstSonhosRewardResponse()

        @Serializable
        class Unauthorized : PutPowerStreamClaimedFirstSonhosRewardResponse()
    }

    @Serializable
    sealed class UpdateLorittaActivityResponse : LorittaDashboardRPCResponse() {
        @Serializable
        class Success : UpdateLorittaActivityResponse()

        @Serializable
        class Unauthorized : UpdateLorittaActivityResponse()
    }

    @Serializable
    sealed class GetSpicyInfoResponse : LorittaDashboardRPCResponse() {
        @Serializable
        class Success(
            // Easter egg
            val comment: String,
            val legacyDashboardUrl: String,
            val clientId: Long,
            val environmentType: EnvironmentType,
            val maxShards: Int,
            val instances: List<PublicLorittaCluster>
        ) : GetSpicyInfoResponse()
    }
}