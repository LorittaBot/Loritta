package net.perfectdreams.loritta.cinnamon.discord.gateway.gateway

import dev.kord.gateway.Gateway
import net.perfectdreams.loritta.cinnamon.discord.gateway.LorittaDiscordGatewayManager

class KordDiscordGatewayManager(
    totalShards: Int,
    override val gateways: Map<Int, Gateway>
) : LorittaDiscordGatewayManager(totalShards) {
    override fun getGatewayForShardOrNull(shardId: Int) = gateways[shardId]
}