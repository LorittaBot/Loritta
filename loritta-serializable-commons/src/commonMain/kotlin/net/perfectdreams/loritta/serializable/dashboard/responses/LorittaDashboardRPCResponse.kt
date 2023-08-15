package net.perfectdreams.loritta.serializable.dashboard.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig

@Serializable
sealed class LorittaDashboardRPCResponse {
    interface UnknownGuildError
    interface UnknownMemberError
    interface MissingPermissionError

    @Serializable
    sealed class GetGuildInfoResponse : LorittaDashboardRPCResponse() {
        @Serializable
        class Success(val guild: DiscordGuild) : GetGuildInfoResponse()

        @Serializable
        class UnknownGuild : GetGuildInfoResponse(), UnknownGuildError

        @Serializable
        class UnknownMember : GetGuildInfoResponse(), UnknownMemberError

        @Serializable
        class MissingPermission : GetGuildInfoResponse(), MissingPermissionError
    }

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
    sealed class GetGuildGamerSaferConfigResponse : LorittaDashboardRPCResponse() {
        @Serializable
        class Success(val config: GuildGamerSaferConfig) : GetGuildGamerSaferConfigResponse()

        @Serializable
        class UnknownGuild : GetGuildGamerSaferConfigResponse(), UnknownGuildError

        @Serializable
        class UnknownMember : GetGuildGamerSaferConfigResponse(), UnknownMemberError

        @Serializable
        class MissingPermission : GetGuildGamerSaferConfigResponse(), MissingPermissionError
    }

    @Serializable
    sealed class UpdateGuildGamerSaferConfigResponse : LorittaDashboardRPCResponse() {
        @Serializable
        class Success : UpdateGuildGamerSaferConfigResponse()

        @Serializable
        class UnknownGuild : UpdateGuildGamerSaferConfigResponse(), UnknownGuildError

        @Serializable
        class UnknownMember : UpdateGuildGamerSaferConfigResponse(), UnknownMemberError

        @Serializable
        class MissingPermission : UpdateGuildGamerSaferConfigResponse(), MissingPermissionError
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
            val authorizationUrl: String
        ) : GetSpicyInfoResponse()
    }
}