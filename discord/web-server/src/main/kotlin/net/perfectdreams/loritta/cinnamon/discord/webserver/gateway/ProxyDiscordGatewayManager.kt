package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import com.zaxxer.hikari.HikariDataSource
import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config.ReplicaInstanceConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding

class ProxyDiscordGatewayManager(
    totalShards: Int,
    replicaInstance: ReplicaInstanceConfig,
    queueDataSource: HikariDataSource
) : LorittaDiscordGatewayManager(totalShards) {
    private val proxiedKordGateways = mutableMapOf<Int, ProxiedKordGateway>()
    override val gateways: Map<Int, ProxiedKordGateway>
        get() = proxiedKordGateways

    init {
        for (shardId in replicaInstance.minShard..replicaInstance.maxShard) {
            proxiedKordGateways[shardId] = ProxiedKordGateway(
                shardId,
                queueDataSource
            )
        }
    }

    override fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId]
}