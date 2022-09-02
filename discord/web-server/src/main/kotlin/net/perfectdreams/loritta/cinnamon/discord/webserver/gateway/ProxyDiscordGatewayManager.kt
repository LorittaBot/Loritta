package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import io.lettuce.core.RedisClient
import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.webserver.LorittaCinnamonWebServer
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig

class ProxyDiscordGatewayManager(
    val loritta: LorittaCinnamonWebServer,
    totalShards: Int,
    replicaInstance: ReplicaInstanceConfig,
    redisClient: RedisClient
) : LorittaDiscordGatewayManager(totalShards) {
    private val proxiedKordGateways = mutableMapOf<Int, ProxiedKordGateway>()
    override val gateways: Map<Int, ProxiedKordGateway>
        get() = proxiedKordGateways

    private val gatewayEventsConnection = redisClient.connect()

    init {
        for (shardId in replicaInstance.minShard..replicaInstance.maxShard) {
            proxiedKordGateways[shardId] = ProxiedKordGateway(
                loritta,
                shardId,
                gatewayEventsConnection
            )
        }
    }

    override fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId]
}