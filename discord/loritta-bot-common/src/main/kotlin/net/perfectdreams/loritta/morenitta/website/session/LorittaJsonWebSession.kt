package net.perfectdreams.loritta.morenitta.website.session

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.utils.gson
import io.ktor.server.application.ApplicationCall
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.toWebSessionIdentification
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.*

data class LorittaJsonWebSession(
		val base64CachedIdentification: String?,
		val base64StoredDiscordAuthTokens: String?
) {
	companion object {
		fun empty() = LorittaJsonWebSession(
				null,
				null
		)

		private val logger = KotlinLogging.logger {}
	}

	suspend fun getUserIdentification(loritta: LorittaBot, call: ApplicationCall, loadFromCache: Boolean = true): UserIdentification? {
		if (loadFromCache) {
			try {
				base64CachedIdentification?.let {
					return gson.fromJson(Base64.getDecoder().decode(it.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8))
				}
			} catch (e: Throwable) {
				logger.error(e) { "Error while loading cached identification for $call" }
			}
		}

		val discordIdentification = getDiscordAuthFromJson(loritta) ?: return null

		try {
			val userIdentification = discordIdentification.getUserIdentification()
			val forCache = userIdentification.toWebSessionIdentification()

			call.lorittaSession = this.copy(
				base64CachedIdentification = Base64.getEncoder().encode(forCache.toJson().toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8)
			)

			return forCache
		} catch (e: Exception) {
			return null
		}
	}

	fun getDiscordAuthFromJson(loritta: LorittaBot): TemmieDiscordAuth? {
		if (base64StoredDiscordAuthTokens == null)
			return null

		val json = try {
			JsonParser.parseString(Base64.getDecoder().decode(base64StoredDiscordAuthTokens.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8))
		} catch (e: Exception) {
			logger.error(e) { "Error while loading cached discord auth" }
			return null
		}

		return TemmieDiscordAuth(
				loritta.config.loritta.discord.applicationId.toString(),
				loritta.config.loritta.discord.clientSecret,
				json["authCode"].string,
				json["redirectUri"].string,
				json["scope"].array.map { it.string },
				json["accessToken"].string,
				json["refreshToken"].string,
				json["expiresIn"].long,
				json["generatedAt"].long
		)
	}

	data class UserIdentification(
			val id: String,
			val username: String,
			val discriminator: String,
			val verified: Boolean,
			val email: String?,
			val avatar: String?,
			val createdAt: Long,
			val updatedAt: Long
	) {
		fun toJson() = gson.toJson(this)
	}
}