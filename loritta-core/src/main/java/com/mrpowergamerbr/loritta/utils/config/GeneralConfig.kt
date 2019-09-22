package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.utils.DiscordUtils

@JsonIgnoreProperties(ignoreUnknown = true)
class GeneralConfig @JsonCreator constructor(
		val loritta: LorittaConfig,
		val clusters: List<LorittaClusterConfig>,
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
	class LorittaClusterConfig @JsonCreator constructor(
			val id: Long,
			val name: String,
			val minShard: Long,
			val maxShard: Long
	) {
		fun getUrl() = DiscordUtils.getUrlForLorittaClusterId(id)
		fun getUserAgent() = Constants.CLUSTER_USER_AGENT.format(id, name)
	}

	class LorittaConfig @JsonCreator constructor(
			val environment: EnvironmentType,
			val featureFlags: List<String>,
			val ownerIds: List<String>,
			val subOwnerIds: List<String>,
			val commands: CommandsConfig,
			val website: WebsiteConfig,
			val clusterReadTimeout: Int,
			val clusterConnectionTimeout: Int
	) {
		class WebsiteConfig @JsonCreator constructor(
				val enabled: Boolean,
				val apiKeys: List<AuthenticationKey>,
				val maxGuildTries: Int,
				val blockedIps: List<String>,
				val blockedUserAgents: List<String>
		) {
			class AuthenticationKey @JsonCreator constructor(
					val name: String,
					val description: String,
					val allowed: List<String>
			)
		}

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