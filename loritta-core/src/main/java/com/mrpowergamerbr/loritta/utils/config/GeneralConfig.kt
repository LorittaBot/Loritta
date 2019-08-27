package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class GeneralConfig @JsonCreator constructor(
		val loritta: LorittaConfig,
		val shards: List<LorittaShardsConfig>,
		@JsonProperty("postgresql")
		val postgreSql: PostgreSqlConfig,
		@JsonProperty("mongodb")
		@Deprecated("MongoDB is unstable")
		val mongoDb: MongoDbConfig,
		val connectionManager: ConnectionManagerConfig,
		@JsonProperty("mercadopago")
		val mercadoPago: MercadoPagoConfig,
		val socket: SocketConfig,
		val youtube: YouTubeConfig,
		val mashape: MashapeConfig,
		val openWeatherMap: OpenWeatherMapConfig,
		val googleVision: GoogleVisionKey,
		val googleRecaptcha: GoogleRecaptcha,
		val github: GitHubConfig,
		val twitter: TwitterConfig,
		val twitch: TwitchConfig,
		val mixer: MixerConfig,
		val fortniteApi: FortniteApiConfig
) {
	class LorittaShardsConfig @JsonCreator constructor(
			val id: Long,
			val name: String,
			val minShard: Long,
			val maxShard: Long
	)

	class LorittaConfig @JsonCreator constructor(
			val environment: EnvironmentType,
			val featureFlags: List<String>,
			val ownerIds: List<String>,
			val subOwnerIds: List<String>,
			val commands: CommandsConfig,
			val folders: FoldersConfig,
			val website: WebsiteConfig
	) {
		class WebsiteConfig @JsonCreator constructor(
				val url: String,
				val folder: String,
				val enabled: Boolean,
				val apiKeys: List<AuthenticationKey>,
				@JsonProperty("port")
				val port: Int
		) {
			class AuthenticationKey @JsonCreator constructor(
					val name: String,
					val description: String,
					val allowed: List<String>
			)
		}

		class FoldersConfig @JsonCreator constructor(
				val root: String,
				val assets: String,
				val temp: String,
				val locales: String,
				val plugins: String,
				val fanArts: String
		)

		class CommandsConfig @JsonCreator constructor(
				val cooldown: Int,
				val imageCooldown: Int,
				val commandsCooldown: Map<String, Int>
		)
	}

	class LorittaAvatarFanArt @JsonCreator constructor(
			val fileName: String,
			val artistId: String,
			val fancyName: String?
	)

	fun isOwner(id: String) = loritta.ownerIds.contains(id)
	fun isOwner(id: Long) = loritta.ownerIds.contains(id.toString())
}