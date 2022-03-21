package net.perfectdreams.loritta.cinnamon.microservices.statscollector.utils

import kotlinx.serialization.Serializable

@Serializable
data class LorittaLegacyStatusResponse(
    val id: Int,
    val name: String,
    val versions: Versions,
    val build: BuildInfo,
    val memory: Memory,
    val threadCount: Int,
    val globalRateLimitHits: Int,
    val isIgnoringRequests: Boolean,
    val pendingMessages: Int,
    val minShard: Int,
    val maxShard: Int,
    val uptime: Long,
    val shards: List<ShardInfo>
) {
    @Serializable
    data class Versions(
        val kotlin: String,
        val java: String,
        val jda: String
    )

    @Serializable
    data class BuildInfo(
        val version: String,
        val buildNumber: String,
        val commitHash: String,
        val gitBranch: String,
        val compiledAt: String,
        val environment: String
    )

    @Serializable
    data class Memory(
        val used: Int,
        val free: Int,
        val max: Int,
        val total: Int
    )

    @Serializable
    data class ShardInfo(
        val id: Int,
        val ping: Int,
        val status: String,
        val guildCount: Int,
        val userCount: Int
    )
}