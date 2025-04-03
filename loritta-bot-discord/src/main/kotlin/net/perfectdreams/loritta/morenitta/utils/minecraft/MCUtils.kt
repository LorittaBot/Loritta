package net.perfectdreams.loritta.morenitta.utils.minecraft

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.LorittaBot.Companion.GSON
import net.perfectdreams.loritta.morenitta.utils.extensions.success
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Classe de utilidades relacionadas ao Minecraft (como UUID query)
 */
object MCUtils {
	val username2uuid = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).maximumSize(10000).build<String, String>().asMap()
	val uuid2profile = Caffeine.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).maximumSize(10000).build<String, MCTextures>().asMap()

	fun getUniqueId(player: String): String? {
		val lowercase = player.lowercase()
		if (username2uuid.contains(lowercase)) {
			return username2uuid[lowercase]
		}

		if (player.isBlank())
			return null

		val payload = JsonArray()
		payload.add(player)

		val connection = HttpRequest.post("https://api.mojang.com/profiles/minecraft")
				.contentType("application/json")
				.send(payload.toString())

		if (!connection.success())
			return null

		val profile = connection.body()
		val array = JsonParser.parseString(profile).array

		array.forEach {
			username2uuid[it["name"].string.lowercase()] = it["id"].string
		}

		return username2uuid[lowercase]
	}

	fun getUserProfileFromName(username: String): MCTextures?  {
		val uuid = getUniqueId(username) ?: return null
		return getUserProfile(uuid)
	}

	fun getUserProfile(uuid: String): MCTextures? {
		if (uuid2profile.contains(uuid))
			return uuid2profile[uuid]

		val connection = HttpRequest.get("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
				.contentType("application/json")

		if (!connection.success())
			return null

		val rawJson = connection.body()
		val profile = JsonParser.parseString(rawJson).obj

		val textureValue = profile["properties"].array.firstOrNull { it["name"].nullString == "textures" }

		if (textureValue == null) {
			uuid2profile[uuid] = null
			return null
		}

		val str = textureValue["value"].string

		val json = String(Base64.getDecoder().decode(str))

		uuid2profile[uuid] = GSON.fromJson(json)
		return uuid2profile[uuid]
	}

	class MCTextures(
			val timestamp: Long,
			val profileId: String,
			val profileName: String,
			val signatureRequired: Boolean?,
			val textures: Map<String, TextureValue>
	)

	class TextureValue(
			val url: String
	)
}