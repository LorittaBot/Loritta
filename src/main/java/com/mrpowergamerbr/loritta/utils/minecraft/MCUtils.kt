package com.mrpowergamerbr.loritta.utils.minecraft

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.common.cache.CacheBuilder
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.utils.jsonParser
import org.jsoup.Jsoup
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Classe de utilidades relacionadas ao Minecraft (como UUID query)
 */
object MCUtils {
	val username2uuid = Caffeine.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).maximumSize(10000).build<String, String?>().asMap()
	val uuid2profile = Caffeine.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).maximumSize(10000).build<String, MCTextures?>().asMap()

	fun getProxy(): Pair<String, Int> {
		val document = Jsoup.connect("https://www.sslproxies.org/").get()
		val trs = document.getElementsByTag("tr")
		val tr = trs[RANDOM.nextInt(trs.size)]
		val tds = tr.getElementsByTag("td")

		val ip = tds[0]
		val port = tds[1]

		return Pair(ip.text(), port.text().toInt())
	}

	fun getUniqueId(player: String): String? {
		val lowercase = player.toLowerCase()
		if (username2uuid.contains(lowercase)) {
			return username2uuid[lowercase]
		}

		val payload = JsonArray()
		payload.add(player)

		// val proxy = getProxy()
		// println("Using proxy $proxy")
		val profile = HttpRequest.post("https://api.mojang.com/profiles/minecraft")
				// .useProxy(proxy.first, proxy.second)
				.contentType("application/json")
				.send(payload.toString())
				.body()

		val array = jsonParser.parse(profile).array

		array.forEach {
			username2uuid[it["name"].string.toLowerCase()] = it["id"].string
		}

		return username2uuid[lowercase]
	}

	fun getUserProfileFromName(username: String): MCTextures?  {
		val uuid = getUniqueId(username) ?: return null
		return getUserProfile(uuid)
	}

	fun getUserProfile(uuid: String): MCTextures? {
		if (uuid2profile.contains(uuid)) {
			return uuid2profile[uuid]
		}

		// val proxy = getProxy()
		// println("Using proxy $proxy")
		val rawJson = HttpRequest.get("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
				// .useProxy(proxy.first, proxy.second)
				.contentType("application/json")
				.body()

		val profile = jsonParser.parse(rawJson).obj

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