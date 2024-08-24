package net.perfectdreams.loritta.apiproxy

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import net.perfectdreams.loritta.serializable.LorittaCluster

class ProxiedRoute(
    val method: HttpMethod,
    val path: String,
    val routeToClusterId: (LoriAPIProxy, ApplicationCall) -> (LorittaCluster)
) {
    companion object {
        val ROUTE_TO_DEFAULT_CLUSTER: (LoriAPIProxy, ApplicationCall) -> (LorittaCluster) = { m, call ->
            m.lorittaInfo.instances.first { it.id == 1 }
        }

        val ROUTE_BASED_ON_GUILD_ID: (LoriAPIProxy, ApplicationCall) -> (LorittaCluster) = { m, call ->
            val shardId = (call.parameters.getOrFail("guildId").toLong() shr 22).rem(m.lorittaInfo.maxShards).toInt()
            m.lorittaInfo.instances.first { shardId in it.minShard..it.maxShard }
        }
    }
}