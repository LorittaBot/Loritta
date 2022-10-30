package net.perfectdreams.loritta.deviouscache.requests

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import net.perfectdreams.loritta.deviouscache.data.*

@Serializable
sealed class DeviousRequest

// ===[ USER ]===
@Serializable
data class GetUserRequest(val id: Snowflake) : DeviousRequest()

@Serializable
data class PutUserRequest(val id: Snowflake, val data: DeviousUserData) : DeviousRequest()

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
data class GetGuildRequest(val id: Snowflake) : DeviousRequest()

@Serializable
data class GetIfGuildExistsRequest(val id: Snowflake) : DeviousRequest()

@Serializable
data class GetGuildWithEntitiesRequest(val id: Snowflake) : DeviousRequest()

@Serializable
data class GetGuildMemberRequest(val guildId: Snowflake, val userId: Snowflake) : DeviousRequest()

@Serializable
data class PutGuildRequest(
    val id: Snowflake,
    val data: DeviousGuildData,
    val roles: List<DeviousRoleData>,
    val emojis: List<DeviousGuildEmojiData>,
    val members: Map<Snowflake, DeviousMemberData>?,
    val channels: List<DeviousChannelData>?,
    val voiceStates: List<DeviousVoiceStateData>?
) : DeviousRequest()

@Serializable
data class PutGuildRoleRequest(
    val guildId: Snowflake,
    val role: DeviousRoleData
) : DeviousRequest()

@Serializable
data class DeleteGuildRoleRequest(val guildId: Snowflake, val roleId: Snowflake) : DeviousRequest()

@Serializable
data class PutGuildEmojisRequest(
    val guildId: Snowflake,
    val emojis: List<DeviousGuildEmojiData>
) : DeviousRequest()

@Serializable
data class GetVoiceStateRequest(
    val guildId: Snowflake,
    val userId: Snowflake
) : DeviousRequest()

@Serializable
data class PutVoiceStateRequest(
    val guildId: Snowflake,
    val userId: Snowflake,
    val channelId: Snowflake?
) : DeviousRequest()

@Serializable
data class DeleteGuildRequest(val id: Snowflake) : DeviousRequest()

@Serializable
data class GetGuildIdsOfShardRequest(
    val shardId: Int,
    val maxShards: Int
) : DeviousRequest()

@Serializable
object GetGuildCountRequest : DeviousRequest()

@Serializable
data class PutGuildMemberRequest(
    val guildId: Snowflake,
    val userId: Snowflake,
    val member: DeviousMemberData
) : DeviousRequest()

@Serializable
data class DeleteGuildMemberRequest(val guildId: Snowflake, val userId: Snowflake) : DeviousRequest()

@Serializable
data class GetGuildMembersRequest(val guildId: Snowflake) : DeviousRequest()

@Serializable
data class GetGuildMembersWithRolesRequest(val guildId: Snowflake, val roles: List<Snowflake>) : DeviousRequest()

@Serializable
data class GetGuildBoostersRequest(val guildId: Snowflake) : DeviousRequest()

// ===[ CHANNEL ]===
@Serializable
data class GetChannelRequest(val channelId: Snowflake) : DeviousRequest()

@Serializable
data class PutChannelRequest(val channelId: Snowflake, val data: DeviousChannelData) : DeviousRequest()

@Serializable
data class DeleteChannelRequest(val channelId: Snowflake) : DeviousRequest()


// ===[ MISCELLANEOUS ]===
@Serializable
data class GetMiscellaneousDataRequest(val key: String) : DeviousRequest()

@Serializable
data class PutMiscellaneousDataRequest(val key: String, val data: String) : DeviousRequest()