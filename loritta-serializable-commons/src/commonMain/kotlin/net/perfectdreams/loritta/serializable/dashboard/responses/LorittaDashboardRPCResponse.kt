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
}