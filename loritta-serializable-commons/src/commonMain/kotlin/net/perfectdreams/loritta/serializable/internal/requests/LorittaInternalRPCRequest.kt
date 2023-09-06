package net.perfectdreams.loritta.serializable.internal.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest

@Serializable
sealed class LorittaInternalRPCRequest {
    @Serializable
    class ExecuteDashGuildScopedRPCRequest(
        val guildId: Long,
        val memberIdToBePermissionCheckedAgainst: Long,
        val dashRequest: DashGuildScopedRequest
    ) : LorittaInternalRPCRequest()

    @Serializable
    class GetLorittaInfoRequest : LorittaInternalRPCRequest()

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