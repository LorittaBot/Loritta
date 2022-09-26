package net.perfectdreams.loritta.legacy.website.utils.extensions

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

fun TemmieDiscordAuth.toJson(): String {
	return gson.toJson(
			jsonObject(
					"authCode" to this.authCode,
					"redirectUri" to this.redirectUri,
					"scope" to this.scope.toJsonArray(),
					"accessToken" to this.accessToken,
					"refreshToken" to this.refreshToken,
					"expiresIn" to this.expiresIn,
					"generatedAt" to this.generatedAt
			)
	)
}

fun TemmieDiscordAuth.UserIdentification.toWebSessionIdentification(): LorittaJsonWebSession.UserIdentification {
	val now = System.currentTimeMillis()

	return LorittaJsonWebSession.UserIdentification(
			id,
			username,
			discriminator,
			verified,
			email,
			avatar,
			now,
			now
	)
}