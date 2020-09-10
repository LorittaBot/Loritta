package com.mrpowergamerbr.loritta.utils.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.utils.DiscordUtils

@JsonIgnoreProperties(ignoreUnknown = true)
class GeneralConfig @JsonCreator constructor(
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
		val github: GitHubConfig,
		val twitter: TwitterConfig,
		val twitch: TwitchConfig,
		val twitch2: TwitchConfig,
		val twitch3: TwitchConfig,
		val twitch4: TwitchConfig,
		val twitch5: TwitchConfig,
		val twitch6: TwitchConfig,
		val twitch7: TwitchConfig,
		val twitch8: TwitchConfig,
		val mixer: MixerConfig,
		val fortniteApi: FortniteApiConfig,
		val caches: CacheConfig
) {
	class LorittaClusterConfig @JsonCreator constructor(
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
				val sessionHex: String,
				val sessionName: String,
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