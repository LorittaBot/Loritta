package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.LorittaDiscordConfig

@Serializable
data class RootConfig(
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val totalShards: Int,
    val replicas: ReplicasConfig,
    val gatewayProxies: List<GatewayProxyConfig>,
    val prometheusPush: PrometheusPushConfig,
    val falatron: FalatronConfig,
    val binaries: BinariesConfig,
    val pudding: PuddingConfig
)