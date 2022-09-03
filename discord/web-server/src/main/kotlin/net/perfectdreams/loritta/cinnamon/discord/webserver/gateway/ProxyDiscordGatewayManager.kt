package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.utils.RedisKeys
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig
import redis.clients.jedis.JedisPool

class ProxyDiscordGatewayManager(
    redisKeys: RedisKeys,
    totalShards: Int,
    replicaInstance: ReplicaInstanceConfig,
    jedisPool: JedisPool
) : LorittaDiscordGatewayManager(totalShards) {
    private val proxiedKordGateways = mutableMapOf<Int, ProxiedKordGateway>()
    override val gateways: Map<Int, ProxiedKordGateway>
        get() = proxiedKordGateways

    init {
        for (shardId in replicaInstance.minShard..replicaInstance.maxShard) {
            proxiedKordGateways[shardId] = ProxiedKordGateway(
                redisKeys,
                shardId,
                jedisPool
            )
        }
    }

    override fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId]
}