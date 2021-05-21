package com.mrpowergamerbr.loritta.utils.config

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.utils.DiscordUtils

@Serializable
class GeneralConfig(
		val loritta: LorittaConfig,
		val clusters: List<LorittaClusterConfig>,
		val database: DatabaseConfig,
		@SerialName("connection-manager")
		val connectionManager: ConnectionManagerConfig,
		@SerialName("perfect-payments")
		val perfectPayments: PerfectPaymentsConfig,
		@SerialName("parallax-code-server")
		val parallaxCodeServer: ParallaxCodeServerConfig,
		val youtube: YouTubeConfig,
		@SerialName("open-weather-map")
		val openWeatherMap: OpenWeatherMapConfig,
		@SerialName("google-vision")
		val googleVision: GoogleVisionKey,
		@SerialName("google-recaptcha")
		val googleRecaptcha: GoogleRecaptcha,
		val crowdin: CrowdinConfig,
		val twitter: TwitterConfig,
		val twitch: TwitchConfig,
		val twitch2: TwitchConfig,
		val twitch3: TwitchConfig,
		val twitch4: TwitchConfig,
		val twitch5: TwitchConfig,
		val twitch6: TwitchConfig,
		val twitch7: TwitchConfig,
		val twitch8: TwitchConfig,
		@SerialName("general-webhook")
		val generalWebhook: GeneralWebhookConfig,
		@SerialName("fortnite-api")
		val fortniteApi: FortniteApiConfig,
		val caches: CacheConfig
) {

	@Serializable
	data class CrowdinConfig(
			val url: String
	)
	@Serializable
	class LorittaClusterConfig(
			val id: Long,
			val name: String,
			@SerialName("min-shard")
			val minShard: Long,
			@SerialName("max-shard")
			val maxShard: Long
	) {
		fun getUrl() = DiscordUtils.getUrlForLorittaClusterId(id)
		fun getUserAgent() = getUserAgent(loritta.config.loritta.environment)
		fun getUserAgent(environmentType: EnvironmentType) = (
				if (environmentType == EnvironmentType.CANARY)
					Constants.CANARY_CLUSTER_USER_AGENT
				else
					Constants.CLUSTER_USER_AGENT
				).format(id, name)
	}
	@Serializable
	data class LorittaConfig(
			val environment: EnvironmentType,
			@SerialName("feature-flags")
			val featureFlags: List<String>,
			@SerialName("owner-ids")
			val ownerIds: List<Long>,
			@SerialName("sub-owner-ids")
			val subOwnerIds: List<Long>,
			val commands: CommandsConfig,
			val website: WebsiteConfig,
			@SerialName("cluster-read-timeout")
			val clusterReadTimeout: Int,
			@SerialName("cluster-connection-timeout")
			val clusterConnectionTimeout: Int
	) {
		@Serializable
		data class WebsiteConfig(
				val enabled: Boolean,
				@SerialName("api-keys")
				val apiKeys: List<AuthenticationKey>,
				@SerialName("max-guild-tries")
				val maxGuildTries: Int,
				@SerialName("session-hex")
				val sessionHex: String,
				@SerialName("session-name")
				val sessionName: String,
				@SerialName("blocked-ips")
				val blockedIps: List<String>,
				@SerialName("blocked-user-agents")
				val blockedUserAgents: List<String>
		) {
			@Serializable
			data class AuthenticationKey (
					val name: String,
					val description: String,
					val allowed: List<String>
			)
		}
		@Serializable
		data class CommandsConfig(
				val cooldown: Int,
				@SerialName("image-cooldown")
				val imageCooldown: Int,
				@SerialName("commands-cooldown")
				val commandsCooldown: Map<String, Int>
		)
	}
	@Serializable
	data class LorittaAvatarFanArt(
			@SerialName("file-name")
			val fileName: String,
			@SerialName("artist-id")
			val artistId: String,
			val fancyName: String? = ""
	)

	fun isOwner(id: String) = loritta.ownerIds.contains(id)
	fun isOwner(id: Long) = loritta.ownerIds.contains(id.toString())
}