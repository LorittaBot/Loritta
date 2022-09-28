package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import io.ktor.server.application.*
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

		val jsonObject = jsonObject(
			"id" to currentShard.id,
			"name" to currentShard.name,
			"versions" to jsonObject(
				"kotlin" to KotlinVersion.CURRENT.toString(),
				"java" to System.getProperty("java.version")
			),
			"memory" to jsonObject(
				"used" to usedMemory,
				"free" to freeMemory,
				"max" to maxMemory,
				"total" to totalMemory
			),
			"threadCount" to ManagementFactory.getThreadMXBean().threadCount,
			"globalRateLimitHits" to loritta.bucketedController?.getGlobalRateLimitHitsInTheLastMinute(),
			"isIgnoringRequests" to loritta.rateLimitChecker.checkIfRequestShouldBeIgnored(),
			"pendingMessages" to loritta.pendingMessages.size,
			"minShard" to currentShard.minShard,
			"maxShard" to currentShard.maxShard,
			"uptime" to ManagementFactory.getRuntimeMXBean().uptime
		)

		val array = jsonArray()

		for (shard in loritta.lorittaShards.shardManager.shards) {
			array.add(
				jsonObject(
					"id" to shard.shardInfo.shardId,
					"ping" to shard.gatewayPing,
					"status" to shard.status.toString(),
					"guildCount" to shard.guildCache.size(),
					"userCount" to shard.userCache.size()
				)
			)
		}

		jsonObject["shards"] = array

		call.respondJson(jsonObject)
	}
}