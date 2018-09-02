package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonProperty
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.entities.Game

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
		@JsonProperty("amino-email")
		val aminoEmail: String,
		@JsonProperty("amino-password")
		val aminoPassword: String,
		@JsonProperty("amino-device-id")
		val aminoDeviceId: String,
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
		@JsonProperty("pomf-space-token")
		val pomfSpaceToken: String,
		@JsonProperty("vagalume-key")
		val vagalumeKey: String,
		@JsonProperty("ghost-ids")
		val ghostIds: List<String>,
		@JsonProperty("fan-art-extravaganza")
		val fanArtExtravaganza: Boolean,
		@JsonProperty("fan-arts")
		val fanArts: List<LorittaAvatarFanArt>,
		@JsonProperty("currently-playing")
		val currentlyPlaying: List<LorittaGameStatus>) {

	class LorittaGameStatus(val name: String, val type: String)

	class LorittaAvatarFanArt(val fileName: String, val artistId: String, val fancyName: String?)

	class AuthenticationKey(val name: String, val description: String, val allowed: List<String>)
}