package net.perfectdreams.loritta.dashboard.backend.configs

import kotlinx.serialization.Serializable

@Serializable
class LorittaDashboardBackendConfig(
    val totalShards: Int,
    val replacers: List<Replacer>,
    val cookieReplacers: List<Replacer>,
    val clusters: List<LorittaClusterConfig>
) {
    @Serializable
    class LorittaClusterConfig(
        val id: Int,
        val name: String,
        val minShard: Int,
        val maxShard: Int,
        val websiteUrl: String,
        val websiteInternalUrl: String,
        val rpcUrl: String,
        val dashboardBaseAPIUrl: String
    )

    @Serializable
    data class Replacer(
        val from: String,
        val to: String
    )
}