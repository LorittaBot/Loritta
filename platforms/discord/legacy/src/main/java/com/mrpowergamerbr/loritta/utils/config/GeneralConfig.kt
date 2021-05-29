package com.mrpowergamerbr.loritta.utils.config

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.utils.DiscordUtils

@Serializable
class GeneralConfig(
		val loritta: LorittaConfig,
		val clusters: List<LorittaClusterConfig>,
		val database: DatabaseConfig,
		val connectionManager: ConnectionManagerConfig,
		val perfectPayments: PerfectPaymentsConfig,
		val parallaxCodeServer: ParallaxCodeServerConfig,
		val youtube: YouTubeConfig,
		val openWeatherMap: OpenWeatherMapConfig,
		val googleVision: GoogleVisionKey,
		val googleRecaptcha: GoogleRecaptcha,
		val crowdin: CrowdinConfig,
		val twitter: TwitterConfig,
		val twitch: TwitchConfig,
		val generalWebhook: GeneralWebhookConfig,
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
			val minShard: Long,
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
			val featureFlags: List<String>,
			val ownerIds: List<Long>,
			val subOwnerIds: List<Long>,
			val commands: CommandsConfig,
			val website: WebsiteConfig,
			val clusterReadTimeout: Int,
			val clusterConnectionTimeout: Int
	) {
		@Serializable
		data class WebsiteConfig(
				val enabled: Boolean,
				val apiKeys: List<AuthenticationKey>,
				val maxGuildTries: Int,
				val sessionHex: String,
				val sessionName: String,
				val blockedIps: List<String>,
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
				val imageCooldown: Int,
				val commandsCooldown: Map<String, Int>
		)
	}
	@Serializable
	data class LorittaAvatarFanArt(
			val fileName: String,
			val artistId: String,
			val fancyName: String? = null
	)

	fun isOwner(id: String) = isOwner(id.toLong())
	fun isOwner(id: Long) = loritta.ownerIds.contains(id)
}