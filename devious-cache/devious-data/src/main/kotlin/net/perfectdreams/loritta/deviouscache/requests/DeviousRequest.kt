package net.perfectdreams.loritta.deviouscache.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.deviouscache.data.*

@Serializable
sealed class DeviousRequest

// ===[ USER ]===
@Serializable
data class GetUserRequest(val id: LightweightSnowflake) : DeviousRequest()

@Serializable
data class PutUserRequest(val id: LightweightSnowflake, val data: DeviousUserData) : DeviousRequest()

// ===[ GATEWAY ]===
@Serializable
data class GetGatewaySessionRequest(val shardId: Int) : DeviousRequest()

@Serializable
data class PutGatewaySessionRequest(
    val shardId: Int,
    val sessionId: String,
    val resumeGatewayUrl: String,
    val sequence: Int
) : DeviousRequest()

@Serializable
data class PutGatewaySequenceRequest(
    val shardId: Int,
    val sequence: Int
) : DeviousRequest()

@Serializable
data class LockConcurrentLoginRequest(val bucket: Int) : DeviousRequest()

@Serializable
data class UnlockConcurrentLoginRequest(val bucket: Int, val key: String) : DeviousRequest()

// ===[ GUILD ]===
@Serializable
data class GetGuildRequest(val id: LightweightSnowflake) : DeviousRequest()

@Serializable
data class GetIfGuildExistsRequest(val id: LightweightSnowflake) : DeviousRequest()

@Serializable
data class GetGuildWithEntitiesRequest(val id: LightweightSnowflake) : DeviousRequest()

@Serializable
data class GetGuildMemberRequest(val guildId: LightweightSnowflake, val userId: LightweightSnowflake) : DeviousRequest()

@Serializable
data class PutGuildRequest(
    val id: LightweightSnowflake,
    val data: DeviousGuildData,
    val roles: List<DeviousRoleData>,
    val emojis: List<DeviousGuildEmojiData>,
    val members: Map<LightweightSnowflake, DeviousMemberData>?,
    val channels: List<DeviousChannelData>?,
    val voiceStates: List<DeviousVoiceStateData>?
) : DeviousRequest()

@Serializable
data class PutGuildsBulkRequest(val requests: List<PutGuildRequest>) : DeviousRequest()

@Serializable
data class PutGuildRoleRequest(
    val guildId: LightweightSnowflake,
    val role: DeviousRoleData
) : DeviousRequest()

@Serializable
data class DeleteGuildRoleRequest(val guildId: LightweightSnowflake, val roleId: LightweightSnowflake) : DeviousRequest()

@Serializable
data class PutGuildEmojisRequest(
    val guildId: LightweightSnowflake,
    val emojis: List<DeviousGuildEmojiData>
) : DeviousRequest()

@Serializable
data class GetVoiceStateRequest(
    val guildId: LightweightSnowflake,
    val userId: LightweightSnowflake
) : DeviousRequest()

@Serializable
data class PutVoiceStateRequest(
    val guildId: LightweightSnowflake,
    val userId: LightweightSnowflake,
    val channelId: LightweightSnowflake?
) : DeviousRequest()

@Serializable
data class DeleteGuildRequest(val id: LightweightSnowflake) : DeviousRequest()

@Serializable
data class GetGuildIdsOfShardRequest(
    val shardId: Int,
    val maxShards: Int
) : DeviousRequest()

@Serializable
object GetGuildCountRequest : DeviousRequest()

@Serializable
data class PutGuildMemberRequest(
    val guildId: LightweightSnowflake,
    val userId: LightweightSnowflake,
    val member: DeviousMemberData
) : DeviousRequest()

@Serializable
data class DeleteGuildMemberRequest(val guildId: LightweightSnowflake, val userId: LightweightSnowflake) : DeviousRequest()

@Serializable
data class GetGuildMembersRequest(val guildId: LightweightSnowflake) : DeviousRequest()

@Serializable
data class GetGuildMembersWithRolesRequest(val guildId: LightweightSnowflake, val roles: List<LightweightSnowflake>) : DeviousRequest()

@Serializable
data class GetGuildBoostersRequest(val guildId: LightweightSnowflake) : DeviousRequest()

// ===[ CHANNEL ]===
@Serializable
data class GetChannelRequest(val channelId: LightweightSnowflake) : DeviousRequest()

@Serializable
data class PutChannelRequest(val channelId: LightweightSnowflake, val data: DeviousChannelData) : DeviousRequest()

@Serializable
data class DeleteChannelRequest(val channelId: LightweightSnowflake) : DeviousRequest()


// ===[ MISCELLANEOUS ]===
@Serializable
data class GetMiscellaneousDataRequest(val key: String) : DeviousRequest()

@Serializable
data class PutMiscellaneousDataRequest(val key: String, val data: String) : DeviousRequest()

@Serializable
object InvokeManualGCRequest : DeviousRequest()
