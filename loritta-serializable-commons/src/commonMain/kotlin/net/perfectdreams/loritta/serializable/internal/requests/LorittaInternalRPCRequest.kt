package net.perfectdreams.loritta.serializable.internal.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig

@Serializable
sealed class LorittaInternalRPCRequest {
    @Serializable
    class GetGuildInfoRequest(
        val guildId: Long,
        val memberIdToBePermissionCheckedAgainst: Long?
    ) : LorittaInternalRPCRequest()

    @Serializable
    class GetLorittaReplicasInfoRequest : LorittaInternalRPCRequest()

    @Serializable
    class GetGuildGamerSaferConfigRequest(
        val guildId: Long,
        val memberIdToBePermissionCheckedAgainst: Long?
    ) : LorittaInternalRPCRequest()

    @Serializable
    class UpdateGuildGamerSaferConfigRequest(
        val guildId: Long,
        val memberIdToBePermissionCheckedAgainst: Long?,
        val config: GuildGamerSaferConfig
    ) : LorittaInternalRPCRequest()
}