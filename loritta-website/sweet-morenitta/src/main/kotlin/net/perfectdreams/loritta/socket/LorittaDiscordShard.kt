package net.perfectdreams.loritta.socket

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging

class LorittaDiscordShard(
    val socketWrapper: SocketWrapper,
    val lorittaShardId: Long,
    val lorittaShardName: String,
    val discordMaxShards: Int,
    val discordShardMin: Int,
    val discordShardMax: Int
) {
    private companion object {
        val logger = KotlinLogging.logger {}
    }

    var shards = mutableMapOf<Int, DiscordShard>()

    fun getTotalGuildCount() = shards.values.sumBy { it.guildCount }
    fun getTotalUserCount() = shards.values.sumBy { it.userCount }

    fun updateStats(jsonNode: JsonNode) {
        jsonNode["shards"].forEach {
            shards[it["id"].intValue()] = DiscordShard(
                it["id"].intValue(),
                it["userCount"].intValue(),
                it["guildCount"].intValue(),
                it["gatewayPing"].longValue(),
                it["status"].asText()
            )
        }

        logger.info("Loritta Discord Shard ${lorittaShardId} (${lorittaShardName}) had its stats updated! Guild count: ${getTotalGuildCount()} - User count: ${getTotalUserCount()}")
    }

    class DiscordShard(
        val id: Int,
        val userCount: Int,
        val guildCount: Int,
        val gatewayPing: Long,
        val status: String
    )
}