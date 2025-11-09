package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import io.ktor.server.application.*
import kotlinx.serialization.json.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import java.lang.management.ManagementFactory

class GetStatusRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/loritta/status") {
	override suspend fun onRequest(call: ApplicationCall) {
		val currentShard = loritta.lorittaCluster

		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()
		val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
		val freeMemory = runtime.freeMemory() / mb
		val maxMemory = runtime.maxMemory() / mb
		val totalMemory = runtime.totalMemory() / mb

		val buildNumber = System.getenv("BUILD_ID")
		val commitHash = System.getenv("COMMIT_HASH")

		val jsonObject = buildJsonObject {
			put("id", currentShard.id)
			put("name", currentShard.name)
			putJsonObject("versions") {
				put("kotlin", KotlinVersion.CURRENT.toString())
				put("java", System.getProperty("java.version"))
			}
			putJsonObject("build") {
				put("buildNumber", buildNumber)
				put("commitHash", commitHash)
				put("environment", loritta.config.loritta.environment.name)
			}
			putJsonObject("memory") {
				put("used", usedMemory)
				put("free", freeMemory)
				put("max", maxMemory)
				put("total", totalMemory)
			}
			put("threadCount", ManagementFactory.getThreadMXBean().threadCount)
			put("pendingMessages", loritta.pendingMessages.size)
			put("minShard", currentShard.minShard)
			put("maxShard", currentShard.maxShard)
			put("uptime", ManagementFactory.getRuntimeMXBean().uptime)
			put("unmodifiedGuilds", loritta.unmodifiedGuilds.size)

			putJsonArray("shards") {
				for (shard in loritta.lorittaShards.shardManager.shards.sortedBy { it.shardInfo.shardId }) {
					addJsonObject {
						put("id", shard.shardInfo.shardId)
						put("ping", shard.gatewayPing)
						put("status", shard.status.toString())
						put("guildCount", shard.guildCache.size())
						put("userCount", shard.userCache.size())
						put("gatewayShardStartupResumeStatus", loritta.gatewayShardsStartupResumeStatus[shard.shardInfo.shardId]?.name)
					}
				}
			}
		}

		call.respondJson(jsonObject)
	}
}