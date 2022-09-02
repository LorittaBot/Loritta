package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig

class ProxyDiscordGatewayManager(
    redisKeys: RedisKeys,
    totalShards: Int,
    replicaInstance: ReplicaInstanceConfig,
    redisConnection: StatefulRedisConnection<String, String>
) : LorittaDiscordGatewayManager(totalShards) {
    private val proxiedKordGateways = mutableMapOf<Int, ProxiedKordGateway>()
    override val gateways: Map<Int, ProxiedKordGateway>
        get() = proxiedKordGateways

    private val gatewayCommandsConnection = redisConnection.coroutines()

    init {
        for (shardId in replicaInstance.minShard..replicaInstance.maxShard) {
            proxiedKordGateways[shardId] = ProxiedKordGateway(
                redisKeys,
                shardId,
                gatewayCommandsConnection
            )
        }
    }

    override fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId]
}