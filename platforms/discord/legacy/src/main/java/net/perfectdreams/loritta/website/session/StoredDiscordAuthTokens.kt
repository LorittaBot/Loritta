package net.perfectdreams.loritta.website.session

data class StoredDiscordAuthTokens(
		val clientId: String,
		val clientSecret: String,
		val authCode: String?,
		val redirectUri: String,
		val scope: List<String>,
		var accessToken: String? = null,
		var refreshToken: String? = null,
		var expiresIn: Long? = null,
		var generatedAt: Long? = null
)