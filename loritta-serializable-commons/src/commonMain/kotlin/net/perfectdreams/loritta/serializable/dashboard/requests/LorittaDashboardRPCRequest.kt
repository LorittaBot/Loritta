package net.perfectdreams.loritta.serializable.dashboard.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig

@Serializable
sealed class LorittaDashboardRPCRequest {
    @Serializable
    class GetGuildInfoRequest(val guildId: Long) : LorittaDashboardRPCRequest()

    @Serializable
    class GetGuildGamerSaferConfigRequest(val guildId: Long) : LorittaDashboardRPCRequest()

    @Serializable
    class UpdateGuildGamerSaferConfigRequest(val guildId: Long, val config: GuildGamerSaferConfig) : LorittaDashboardRPCRequest()
}