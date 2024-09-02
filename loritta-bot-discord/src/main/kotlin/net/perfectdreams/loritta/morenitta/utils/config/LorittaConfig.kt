package net.perfectdreams.loritta.morenitta.utils.config

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.EnvironmentType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils

@Serializable
data class LorittaConfig(
    val environment: EnvironmentType,
    val ownerIds: List<Snowflake>,
    val clusterReadTimeout: Int,
    val clusterConnectionTimeout: Int,
    val webhookSecret: String,
    val discord: DiscordConfig,
    val interactions: InteractionsConfig,
    val clusters: LorittaClustersConfig,
    val folders: FoldersConfig,
    val commands: CommandsConfig,
    val website: WebsiteConfig,
    val pudding: PuddingConfig,
    val perfectPayments: PerfectPaymentsConfig,
    val binaries: BinariesConfig,
    val falatron: FalatronConfig,
    val gabrielaImageServer: GabrielaImageServerConfig,
    val dreamStorageService: DreamStorageServiceConfig,
    val etherealGambiService: EtherealGambiServiceConfig,
    val gabrielaHelperService: GabrielaHelperServiceConfig,
    val randomRoleplayPictures: RandomRoleplayPicturesConfig,
    val youtube: YouTubeConfig,
    val openWeatherMap: OpenWeatherMapConfig,
    val googleVision: GoogleVisionConfig,
    val googleRecaptcha: GoogleRecaptchaConfig,
    val turnstileCaptchas: TurnstileCaptchas,
    val voteWebsites: VotingWebsites,
    val messageEncryption: MessageEncryptionConfig,
    val messageVerification: MessageVerificationConfig,
    val messageRenderer: MessageRendererConfig,
    val sparklyPower: SparklyPowerConfig,
    val crowdin: CrowdinConfig,
    val twitch: TwitchConfig,
    val bluesky: BlueskyConfig,
    val quirky: QuirkyConfig,
    val donatorsOstentation: DonatorsOstentationConfig,
    val connectionManager: ConnectionManagerConfig,
    val antiRaidIds: List<Snowflake>
) {
    @Serializable
    data class DiscordConfig(
        val token: String,
        val applicationId: Snowflake,
        val clientSecret: String,
        val maxShards: Int,
        val maxRequestsPerHost: Int,
        val maxConcurrency: Int,
        val maxParallelLogins: Int,
        val okHttp: JdaOkHttpConfig,
        val requestLimiter: RequestLimiterConfig,
        val baseUrl: String?
    ) {
        @Serializable
        data class JdaOkHttpConfig(
            val readTimeout: Long,
            val connectTimeout: Long,
            val writeTimeout: Long
        )

        @Serializable
        data class RequestLimiterConfig(
            val enabled: Boolean,
            val maxRequestsPer10Minutes: Int,
            val consoleWarnCooldown: Long,
            val removePendingRequestsCooldown: Long
        )
    }

    @Serializable
    data class InteractionsConfig(
        val registerGlobally: Boolean,
        val guildsToBeRegistered: List<Snowflake>
    )

    @Serializable
    class LorittaClustersConfig(
        val getClusterIdFromHostname: Boolean,
        val clusterIdOverride: Int? = null,
        val instances: List<LorittaClusterConfig>
    ) {
        @Serializable
        class LorittaClusterConfig(
            val id: Int,
            val name: String,
            val minShard: Int,
            val maxShard: Int,
            val websiteUrl: String,
            val rpcUrl: String
        ) {
            fun getUrl(loritta: LorittaBot) = DiscordUtils.getUrlForLorittaClusterId(loritta, id)
            fun getUserAgent(loritta: LorittaBot) = getUserAgent(loritta.config.loritta.environment)
            fun getUserAgent(environmentType: EnvironmentType) = (
                    if (environmentType == EnvironmentType.CANARY)
                        Constants.CANARY_CLUSTER_USER_AGENT
                    else
                        Constants.CLUSTER_USER_AGENT
                    ).format(id, name)
        }
    }

    @Serializable
    data class FoldersConfig(
        val root: String,
        val assets: String,
        val temp: String,
        val website: String,
        val content: String
    )

    @Serializable
    data class CommandsConfig(
        val cooldown: Int,
        val imageCooldown: Int,
        val commandsCooldown: Map<String, Int>
    )

    @Serializable
    data class WebsiteConfig(
        val url: String,
        val port: Int,
        val spicyMorenittaDashboardUrl: String,
        val apiKeys: List<AuthenticationKey>,
        val maxGuildTries: Int,
        val sessionHex: String,
        val sessionName: String,
        val sessionDomain: String,
        val spicyMorenittaJsPath: String?,
    ) {
        @Serializable
        data class AuthenticationKey (
            val name: String,
            val description: String,
            val allowed: List<String>
        )
    }

    @Serializable
    data class PuddingConfig(
        val database: String,
        val address: String,
        val username: String,
        val password: String
    )

    @Serializable
    data class PerfectPaymentsConfig(
        val url: String,
        val notificationToken: String,
        val token: String
    )

    @Serializable
    data class BinariesConfig(
        val ffmpeg: String,
        val gifsicle: String
    )

    @Serializable
    data class FalatronConfig(
        val url: String,
        val key: String
    )

    @Serializable
    data class GabrielaImageServerConfig(val url: String)

    @Serializable
    data class DreamStorageServiceConfig(
        val url: String,
        val token: String
    )

    @Serializable
    data class EtherealGambiServiceConfig(val url: String)

    @Serializable
    data class GabrielaHelperServiceConfig(val url: String)

    @Serializable
    data class RandomRoleplayPicturesConfig(val url: String)

    @Serializable
    data class YouTubeConfig(val key: String)

    @Serializable
    data class OpenWeatherMapConfig(val key: String)

    @Serializable
    data class GoogleVisionConfig(val key: String)

    @Serializable
    data class GoogleRecaptchaConfig(
        val serverVoteToken: String,
        val reputationToken: String
    )

    @Serializable
    data class TurnstileCaptchas(
        val dailyReward: CloudflareTurnstileConfig
    )

    @Serializable
    data class CloudflareTurnstileConfig(
        val siteKey: String,
        val secretKey: String
    )

    @Serializable
    data class VotingWebsites(
        val topgg: TopggConfig,
        val discordBots: DiscordBotsConfig
    )

    @Serializable
    data class TopggConfig(
        val clientId: Long,
        val token: String
    )

    @Serializable
    data class DiscordBotsConfig(
        val clientId: Long,
        val token: String
    )

    @Serializable
    data class CrowdinConfig(val url: String)

    @Serializable
    data class MessageEncryptionConfig(val encryptionKey: String)

    @Serializable
    data class MessageVerificationConfig(val encryptionKey: String)

    @Serializable
    data class MessageRendererConfig(val rendererUrl: String)

    @Serializable
    data class SparklyPowerConfig(val sparklySurvivalUrl: String)

    @Serializable
    data class TwitchConfig(
        val clientId: String,
        val clientSecret: String,
        val webhookUrl: String,
        val webhookSecret: String
    )

    @Serializable
    data class BlueskyConfig(
        val firehoseEnabledOnClusterId: Int
    )

    @Serializable
    class QuirkyConfig(
        val randomReactions: RandomReactionsConfig,
        val tioDoPave: TioDoPaveConfig,
        val canecaUsers: List<Snowflake>
    ) {
        @Serializable
        class RandomReactionsConfig(
            val enabled: Boolean,
            val maxBound: Int,
            val reactions: List<String>,
            val contextAwareReactions: List<ContextAwareReaction>
        ) {
            @Serializable
            class ContextAwareReaction(
                val match: String,
                val chanceOf: Double,
                val reactions: List<String>
            )
        }
        @Serializable
        class TioDoPaveConfig(
            val enabled: Boolean,
            val chance: Double
        )
    }

    @Serializable
    class DonatorsOstentationConfig(
        val boostEnabledGuilds: List<BoostEnabledGuild>,
        val boostMax: Int,
        val automaticallyUpdateMessage: Boolean,
        val channelId: Long,
        val messageId: Long
    ) {
        @Serializable
        class BoostEnabledGuild(
            val id: Long,
            val inviteId: String,
            val priority: Int
        )
    }

    @Serializable
    data class ConnectionManagerConfig(
        val trustedDomains: List<String>,
        val blockedDomains: List<String>
    )
}