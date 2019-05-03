package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class LorittaConfig(
		@JsonProperty("loritta")
		val loritta: LorittaConfig,
		@JsonProperty("discord")
		val discord: DiscordConfig,
		@JsonProperty("postgresql")
		val postgreSql: PostgreSqlConfig,
		@JsonProperty("mongodb")
		@Deprecated("MongoDB is unstable")
		val mongoDb: MongoDbConfig,
		@JsonProperty("lavalink")
		val lavalink: LavalinkConfig,
		@JsonProperty("connection-manager")
		val connectionManager: ConnectionManagerConfig,
		@JsonProperty("mercadopago")
		val mercadoPago: MercadoPagoConfig,
		@JsonProperty("socket")
		val socket: SocketConfig,
		@JsonProperty("youtube")
		val youtube: YouTubeConfig,
		@JsonProperty("mashape")
		val mashape: MashapeConfig,
		@JsonProperty("open-weather-map")
		val openWeatherMap: OpenWeatherMapConfig,
		@JsonProperty("google-vision")
		val googleVision: GoogleVisionKey,
		@JsonProperty("google-recaptcha")
		val googleRecaptcha: GoogleRecaptcha,
		@JsonProperty("github")
		val github: GitHubConfig,
		@JsonProperty("twitter")
		val twitter: TwitterConfig,
		@JsonProperty("twitch")
		val twitch: TwitchConfig,
		@JsonProperty("mixer")
		val mixer: MixerConfig,
		@JsonProperty("discord-bots")
		val discordBots: DiscordBotsConfig,
		@JsonProperty("discord-bot-list")
		val discordBotList: DiscordBotListConfig,
		@JsonProperty("ghost-ids")
		val ghostIds: List<String>,
		@JsonProperty("anti-raid-ids")
		val antiRaidIds: List<String>) {

	class LorittaConfig(
			@JsonProperty("environment")
			val environment: EnvironmentType,
			@JsonProperty("feature-flags")
			val featureFlags: List<String>,
			@JsonProperty("owner-ids")
			val ownerIds: List<String>,
			@JsonProperty("sub-owner-ids")
			val subOwnerIds: List<String>,
			@JsonProperty("folders")
			val folders: FoldersConfig,
			@JsonProperty("website")
			val website: WebsiteConfig
	) {
		class WebsiteConfig(
				@JsonProperty("url")
				val url: String,
				@JsonProperty("folder")
				val folder: String,
				@JsonProperty("enabled")
				val enabled: Boolean,
				@JsonProperty("api-keys")
				val apiKeys: List<AuthenticationKey>,
				@JsonProperty("port")
				val port: Int
		) {
			class AuthenticationKey(
					@JsonProperty("name")
					val name: String,
					@JsonProperty("description")
					val description: String,
					@JsonProperty("allowed")
					val allowed: List<String>
			)
		}

		class FoldersConfig(
				@JsonProperty("root")
				val root: String,
				@JsonProperty("assets")
				val assets: String,
				@JsonProperty("temp")
				val temp: String,
				@JsonProperty("locales")
				val locales: String
		)
	}

	class LorittaAvatarFanArt(
			@JsonProperty("file-name")
			val fileName: String,
			@JsonProperty("artist-id")
			val artistId: String,
			@JsonProperty("fancy-name")
			val fancyName: String?
	)

	fun isOwner(id: String) = loritta.ownerIds.contains(id)
	fun isOwner(id: Long) = loritta.ownerIds.contains(id.toString())
}