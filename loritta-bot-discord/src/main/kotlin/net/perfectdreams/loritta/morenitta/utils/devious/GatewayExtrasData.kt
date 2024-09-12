package net.perfectdreams.loritta.morenitta.utils.devious

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class GatewayExtrasData(
    /**
     * When the gateway session started to be shutted down, used for debugging and stats
     */
    val shutdownBeganAt: Instant,

    /**
     * When the gateway session finished to be shutted down after writing the shard data to disk, used for debugging and stats
     */
    val shutdownFinishedAt: Instant,

    /**
     * The DeviousConverter version used for the cached guilds
     */
    val deviousConverterVersion: Int
)