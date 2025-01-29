package net.perfectdreams.loritta.publichttpapi

import io.ktor.http.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

object LoriPublicHttpApiEndpoints {
    val GET_USER_BY_ID = LoriPublicHttpApiEndpoint(
        HttpMethod.Get,
        "/users/{userId}",
    )
    val GET_USER_TRANSACTIONS = LoriPublicHttpApiEndpoint(HttpMethod.Get, "/users/{userId}/transactions")
    val GET_SONHOS_RANK = LoriPublicHttpApiEndpoint(HttpMethod.Get, "/sonhos/rank")
    val GET_THIRD_PARTY_SONHOS_TRANSFER_STATUS = LoriPublicHttpApiEndpoint(HttpMethod.Get, "/sonhos/third-party-sonhos-transfer/{sonhosTransferId}")

    val VERIFY_LORITTA_MESSAGE = LoriPublicHttpApiEndpoint(HttpMethod.Post, "/lori-messages/verify-message")
    val SAVE_LORITTA_MESSAGE = LoriPublicHttpApiEndpoint(HttpMethod.Post, "/guilds/{guildId}/channels/{channelId}/messages/{messageId}/save-lori-message")

    val CREATE_GUILD_GIVEAWAY = LoriPublicHttpApiEndpoint(HttpMethod.Put, "/guilds/{guildId}/giveaways")
    val END_GUILD_GIVEAWAY = LoriPublicHttpApiEndpoint(HttpMethod.Post, "/guilds/{guildId}/giveaways/{giveawayId}/end")
    val REROLL_GUILD_GIVEAWAY = LoriPublicHttpApiEndpoint(HttpMethod.Post, "/guilds/{guildId}/giveaways/{giveawayId}/reroll")
    val TRANSFER_SONHOS = LoriPublicHttpApiEndpoint(HttpMethod.Post, "/guilds/{guildId}/channels/{channelId}/sonhos/sonhos-transfer")
    val REQUEST_SONHOS = LoriPublicHttpApiEndpoint(HttpMethod.Post, "/guilds/{guildId}/channels/{channelId}/sonhos/sonhos-request")

    val EMOJIFIGHT_GUILD_TOP_WINNERS_RANK = LoriPublicHttpApiEndpoint(HttpMethod.Get, "/guilds/{guildId}/emojifights/top-winners")
    val EMOJIFIGHT_GUILD_VICTORIES = LoriPublicHttpApiEndpoint(HttpMethod.Get, "/guilds/{guildId}/members/{userId}/emojifight/victories")

    val CREATE_GUILD_MUSICALCHAIRS = LoriPublicHttpApiEndpoint(HttpMethod.Post, "/guilds/{guildId}/musical-chairs")

    val all by lazy {
        LoriPublicHttpApiEndpoints::class.declaredMemberProperties
            .filter {
                it.returnType.isSubtypeOf(LoriPublicHttpApiEndpoint::class.starProjectedType)
            }
            .map { it.call(this) } as List<LoriPublicHttpApiEndpoint>
    }

    fun getEndpointById(endpointId: String): LoriPublicHttpApiEndpoint {
        val callable = LoriPublicHttpApiEndpoints::class.declaredMembers
            .firstOrNull { it.name == endpointId }
        if (callable == null)
            error("Unknown endpoint $endpointId")

        return callable.call(LoriPublicHttpApiEndpoints) as LoriPublicHttpApiEndpoint
    }
}