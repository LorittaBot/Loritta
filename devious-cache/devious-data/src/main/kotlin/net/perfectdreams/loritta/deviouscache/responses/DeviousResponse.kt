package net.perfectdreams.loritta.deviouscache.responses

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviouscache.requests.DeviousRequest

@Serializable
sealed class DeviousResponse

@Serializable
object NotFoundResponse : DeviousResponse()

@Serializable
object OkResponse : DeviousResponse()

// ===[ GATEWAY ]===
@Serializable
data class GetGatewaySessionResponse(
    val sessionId: String,
    val resumeGatewayUrl: String,
    val sequence: Int
) : DeviousResponse()

@Serializable
class LockSuccessfulConcurrentLoginResponse(val key: String) : DeviousResponse()

@Serializable
object LockConflictConcurrentLoginResponse : DeviousResponse()

@Serializable
object UnlockConflictConcurrentLoginResponse : DeviousResponse()

// ===[ USER ]===
@Serializable
data class GetUserResponse(val user: DeviousUserData) : DeviousResponse()

// ===[ GUILD ]===
@Serializable
data class GetGuildResponse(val data: DeviousGuildData) : DeviousResponse()

@Serializable
data class GetGuildWithEntitiesResponse(
    val data: DeviousGuildData,
    val roles: Map<Snowflake, DeviousRoleData>,
    val channels: Map<Snowflake, DeviousChannelData>,
    val emojis: Map<Snowflake, DeviousGuildEmojiData>
) : DeviousResponse()

@Serializable
data class GetGuildMemberResponse(val member: DeviousMemberData) : DeviousResponse()

@Serializable
data class GetGuildIdsOfShardResponse(val guildIds: List<Snowflake>) : DeviousResponse()

@Serializable
data class GetGuildCountResponse(val count: Long) : DeviousResponse()

@Serializable
data class PutGuildMemberResponse(
    val oldMember: DeviousMemberData?,
    val newMember: DeviousMemberData
) : DeviousResponse()

@Serializable
data class GetGuildMembersResponse(
    val members: Map<Snowflake, DeviousUserAndMember>
) : DeviousResponse()

@Serializable
data class GetVoiceStateResponse(val channelId: Snowflake?) : DeviousResponse()

@Serializable
data class GetChannelResponse(val channel: DeviousChannelData) : DeviousResponse()

@Serializable
data class GetGuildChannelResponse(
    val channel: DeviousChannelData,
    val data: DeviousGuildData,
    val roles: Map<Snowflake, DeviousRoleData>,
    val channels: Map<Snowflake, DeviousChannelData>,
    val emojis: Map<Snowflake, DeviousGuildEmojiData>
) : DeviousResponse()

// ===[ MISCELLANEOUS ]===
@Serializable
data class GetMiscellaneousDataResponse(val data: String) : DeviousResponse()