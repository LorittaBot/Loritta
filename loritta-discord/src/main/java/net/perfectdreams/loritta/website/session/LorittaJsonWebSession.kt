package net.perfectdreams.loritta.website.session

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.application.ApplicationCall
import mu.KotlinLogging
import net.perfectdreams.loritta.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.website.utils.extensions.toWebSessionIdentification
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

data class LorittaJsonWebSession(
		val cachedIdentification: String?,
		val storedDiscordAuthTokens: String?
) {
	companion object {
		fun empty() = LorittaJsonWebSession(
				null,
				null
		)

		private val logger = KotlinLogging.logger {}
	}

	suspend fun getUserIdentification(call: ApplicationCall, loadFromCache: Boolean = true): UserIdentification? {
		if (loadFromCache) {
			try {
				cachedIdentification?.let {
					return gson.fromJson(it)
				}
			} catch (e: Throwable) {
				logger.error(e) { "Error while loading cached identification for $call" }
			}
		}

		val discordIdentification = getDiscordAuthFromJson() ?: return null

		try {
			val userIdentification = discordIdentification.getUserIdentification()
			val forCache = userIdentification.toWebSessionIdentification()

			call.lorittaSession = this.copy(
					cachedIdentification = forCache.toJson()
			)

			return forCache
		} catch (e: Exception) {
			return null
		}
	}

	fun getDiscordAuthFromJson(): TemmieDiscordAuth? {
		if (storedDiscordAuthTokens == null)
			return null

		val json = JsonParser.parseString(storedDiscordAuthTokens)

		return TemmieDiscordAuth(
				loritta.discordConfig.discord.clientId,
				loritta.discordConfig.discord.clientSecret,
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