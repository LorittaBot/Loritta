package net.perfectdreams.loritta.website.session

import com.github.salomonbrys.kotson.*
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.jsonParser
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.website.utils.extensions.lorittaSession
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
	}

	suspend fun getUserIdentification(call: ApplicationCall, loadFromCache: Boolean = true): UserIdentification? {
		println("getUserIdentification($call, $loadFromCache)")
		if (loadFromCache) {
			println("Trying to load from cache...")
			cachedIdentification?.let {
				println(it + " -> " + gson.fromJson(it))
				return gson.fromJson(it)
			}
		}

		val discordIdentification = getDiscordAuthFromJson() ?: return null

		println("Wasn't able to load from cache... :(")
		try {
			val userIdentification = discordIdentification.getUserIdentification()
			val forCache = UserIdentification(
					userIdentification.id,
					userIdentification.username,
					userIdentification.discriminator,
					userIdentification.verified,
					userIdentification.email
			)

			call.lorittaSession = this.copy(
					cachedIdentification = gson.toJson(
							forCache
					)
			)

			return forCache
		} catch (e: Exception) {
			return null
		}
	}

	fun getDiscordAuthFromJson(): TemmieDiscordAuth? {
		if (storedDiscordAuthTokens == null)
			return null

		val json = jsonParser.parse(storedDiscordAuthTokens)

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
			val email: String?
	)
}