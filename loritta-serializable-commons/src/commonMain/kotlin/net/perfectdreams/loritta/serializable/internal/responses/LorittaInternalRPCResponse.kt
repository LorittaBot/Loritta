package net.perfectdreams.loritta.serializable.internal.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig

@Serializable
sealed class LorittaInternalRPCResponse {
    interface UnknownGuildError
    interface UnknownMemberError
    interface MissingPermissionError

    @Serializable
    sealed class GetGuildInfoResponse : LorittaInternalRPCResponse() {
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
    sealed class GetLorittaReplicasInfoResponse : LorittaInternalRPCResponse() {
        @Serializable
        class Success(
            val maxShards: Int,
            val instances: List<LorittaCluster>
        ) : GetLorittaReplicasInfoResponse()

        @Serializable
        data class LorittaCluster(
            val id: Int,
            val name: String,
            val minShard: Int,
            val maxShard: Int,
            val websiteUrl: String,
            val rpcUrl: String
        )
    }

    @Serializable
    sealed class GetGuildGamerSaferConfigResponse : LorittaInternalRPCResponse() {
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
    sealed class UpdateGuildGamerSaferConfigResponse : LorittaInternalRPCResponse() {
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