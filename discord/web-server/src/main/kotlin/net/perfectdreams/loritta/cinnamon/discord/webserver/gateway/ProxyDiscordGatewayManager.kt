package net.perfectdreams.loritta.cinnamon.discord.webserver.gateway

import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.gatewayproxy.GatewayProxy
import net.perfectdreams.loritta.cinnamon.discord.webserver.gateway.gatewayproxy.ProxiedKordGateway

class ProxyDiscordGatewayManager(
    totalShards: Int,
    val gatewayProxies: List<GatewayProxy>
) : LorittaDiscordGatewayManager(totalShards) {
    override val gateways: Map<Int, ProxiedKordGateway>
        get() = gatewayProxies.flatMap { it.proxiedKordGateways.entries }.associate { it.key to it.value }

    override fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId]
}