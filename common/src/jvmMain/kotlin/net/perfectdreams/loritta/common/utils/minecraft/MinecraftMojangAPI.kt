package net.perfectdreams.loritta.common.utils.minecraft

import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Wrapper for Mojang's Minecraft API, useful for UUID queries and similar
 *
 * https://wiki.vg/Mojang_API
 */
class MinecraftMojangAPI {
    companion object {
        private val http = HttpClient {
            expectSuccess = false
        }

        private val json = Json {
            ignoreUnknownKeys = true
        }
    }

    private val username2uuid = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).maximumSize(10000).build<String, UUID?>().asMap()
    private val uuid2profile = Caffeine.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).maximumSize(10000).build<UUID, MCTextures?>().asMap()

    suspend fun getUniqueId(player: String): UUID? {
        val lowercase = player.toLowerCase()
        if (username2uuid.contains(lowercase)) {
            return username2uuid[lowercase]
        }

        if (player.isBlank())
            return null

        val connection = http.get<HttpResponse>("https://api.mojang.com/users/profiles/minecraft/$player")

        // Mojang uses "204 No Content" if the account doesn't exist
        if (connection.status != HttpStatusCode.OK)
            return null

        val profile = connection.readText()
        val obj = json.parseToJsonElement(profile).jsonObject
        username2uuid[obj["name"]!!.jsonPrimitive.content.toLowerCase()] = convertNonDashedToUniqueID(obj["id"]!!.jsonPrimitive.content)

        return username2uuid[lowercase]
    }

    suspend fun getUserProfileFromName(username: String): MCTextures? {
        val uuid = getUniqueId(username) ?: return null
        return getUserProfile(uuid)
    }

    suspend fun getUserProfile(uuid: UUID): MCTextures? {
        if (uuid2profile.contains(uuid))
            return uuid2profile[uuid]

        val connection = http.get<HttpResponse>("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")

        if (!connection.status.isSuccess())
            return null

        val rawJson = connection.readText()
        val profile = json.parseToJsonElement(rawJson).jsonObject

        val textureValue = profile["properties"]!!
            .jsonArray
            .firstOrNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull == "textures" }
            ?.jsonObject

        if (textureValue == null) {
            uuid2profile[uuid] = null
            return null
        }

        val str = textureValue["value"]?.jsonPrimitive?.content

        val json = String(Base64.getDecoder().decode(str))

        uuid2profile[uuid] = MinecraftMojangAPI.json.decodeFromString(json)
        return uuid2profile[uuid]
    }

    fun convertNonDashedToUniqueID(id: String): UUID {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32))
    }

    @Serializable
    class MCTextures(
        val timestamp: Long,
        val profileId: String,
        val profileName: String,
        val signatureRequired: Boolean? = null,
        val textures: Map<String, TextureValue>
    )

    @Serializable
    class TextureValue(
        val url: String
    )
}