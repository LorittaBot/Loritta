package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty
import net.dv8tion.jda.core.OnlineStatus

class LorittaConfig(
		@JsonProperty("mongodb-ip")
		val mongoDbIp: String,
		@JsonProperty("lavalink-ip")
		val lavalinkIp: String,
		@JsonProperty("shards")
		val shards: Int,
		@JsonProperty("client-token")
		val clientToken: String,
		@JsonProperty("client-id")
		val clientId: String,
		@JsonProperty("client-secret")
		val clientSecret: String,
		@JsonProperty("user-status")
		val userStatus: OnlineStatus,
		@JsonProperty("database-name")
		val databaseName: String,
		@JsonProperty("postgresql-ip")
		val postgreSqlIp: String,
		@JsonProperty("postgresql-port")
		val postgreSqlPort: String,
		@JsonProperty("postgresql-user")
		val postgreSqlUser: String,
		@JsonProperty("postgresql-password")
		val postgreSqlPassword: String,
		@JsonProperty("environment")
		val environment: EnvironmentType,
		@JsonProperty("youtube-keys")
		val youtubeKeys: List<String>,
		@JsonProperty("website-api-keys")
		val websiteApiKeys: List<AuthenticationKey>,
		@JsonProperty("owner-id")
		val ownerId: String,
		@JsonProperty("website-url")
		val websiteUrl: String,
		@JsonProperty("website-port")
		val websitePort: Int,
		@JsonProperty("socket-port")
		val socketPort: Int,
		@JsonProperty("loritta-folder")
		val lorittaFolder: String,
		@JsonProperty("assets-folder")
		val assetsFolder: String,
		@JsonProperty("temp-folder")
		val tempFolder: String,
		@JsonProperty("locales-folder")
		val localesFolder: String,
		@JsonProperty("frontend-folder")
		val frontendFolder: String,
		@JsonProperty("authorization-url")
		val authorizationUrl: String,
		@JsonProperty("add-bot-url")
		val addBotUrl: String,
		@JsonProperty("mercadopago-client-id")
		val mercadoPagoClientId: String,
		@JsonProperty("mercadopago-client-token")
		val mercadoPagoClientToken: String,
		@JsonProperty("mashape-key")
		val mashapeKey: String,
		@JsonProperty("discord-bots-key")
		val discordBotsKey: String,
		@JsonProperty("discord-bot-list-key")
		val discordBotsOrgKey: String,
		@JsonProperty("vespertine-bot-list-key")
		val vespertineBotListKey: String,
		@JsonProperty("open-weather-map-key")
		val openWeatherMapKey: String,
		@JsonProperty("facebook-key")
		val facebookToken: String,
		@JsonProperty("google-vision-key")
		val googleVisionKey: String,
		@JsonProperty("twitch-client-id")
		val twitchClientId: String,
		@JsonProperty("mixer-client-id")
		val mixerClientId: String,
		@JsonProperty("mixer-client-secret")
		val mixerClientSecret: String,
		@JsonProperty("mixer-webhook-secret")
		val mixerWebhookSecret: String,
		@JsonProperty("recaptcha-token")
		val recaptchaToken: String,
		@JsonProperty("invisible-recaptcha-token")
		val invisibleRecaptchaToken: String,
		@JsonProperty("pomf-space-token")
		val pomfSpaceToken: String,
		@JsonProperty("vagalume-key")
		val vagalumeKey: String,
		@JsonProperty("twitter")
		val twitterConfig: TwitterConfig,
		@JsonProperty("connection-manager")
		val connectionManagerConfig: ConnectionManagerConfig,
		@JsonProperty("ghost-ids")
		val ghostIds: List<String>,
		@JsonProperty("anti-raid-ids")
		val antiRaidIds: List<String>,
		@JsonProperty("fan-art-extravaganza")
		val fanArtExtravaganza: Boolean,
		@JsonProperty("fan-arts")
		val fanArts: List<LorittaAvatarFanArt>,
		@JsonProperty("currently-playing")
		val currentlyPlaying: List<LorittaGameStatus>) {

	class LorittaGameStatus(
			@JsonProperty("name")
			val name: String,
			@JsonProperty("type")
			val type: String
	)

	class LorittaAvatarFanArt(
			@JsonProperty("file-name")
			val fileName: String,
			@JsonProperty("artist-id")
			val artistId: String,
			@JsonProperty("fancy-name")
			val fancyName: String?
	)

	class AuthenticationKey(
			@JsonProperty("name")
			val name: String,
			@JsonProperty("description")
			val description: String,
			@JsonProperty("allowed")
			val allowed: List<String>
	)

	class TwitterConfig(
			@JsonProperty("consumer-key")
			val oAuthConsumerKey: String,
			@JsonProperty("consumer-secret")
			val oAuthConsumerSecret: String,
			@JsonProperty("access-token")
			val oAuthAccessToken: String,
			@JsonProperty("access-token-secret")
			val oAuthAccessTokenSecret: String
	)

	class ConnectionManagerConfig(
			@JsonProperty("trusted-domains")
			val trustedDomains: List<String>,
			@JsonProperty("proxy-ip")
			val proxyIp: String,
			@JsonProperty("proxy-port")
			val proxyPort: Int
	)
}