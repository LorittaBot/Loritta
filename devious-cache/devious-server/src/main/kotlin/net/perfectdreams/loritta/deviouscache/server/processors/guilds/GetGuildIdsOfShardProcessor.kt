package net.perfectdreams.loritta.deviouscache.server.processors.guilds

import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.LightweightSnowflake
import net.perfectdreams.loritta.deviouscache.requests.GetGuildIdsOfShardRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.GetGuildIdsOfShardResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache

class GetGuildIdsOfShardProcessor(val m: DeviousCache) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun process(request: GetGuildIdsOfShardRequest): DeviousResponse {
        logger.info { "Getting guild IDs of shard ${request.shardId}" }

        val guildsOnThisShard = m.guilds.keys.filter { (it shr 22).rem(request.maxShards).toInt() == request.shardId }

        return GetGuildIdsOfShardResponse(guildsOnThisShard.map { LightweightSnowflake(it) })
    }
}