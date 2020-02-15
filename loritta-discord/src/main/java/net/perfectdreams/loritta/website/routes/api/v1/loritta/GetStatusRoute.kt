package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.lang.management.ManagementFactory

class GetStatusRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/status") {
	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
		val currentShard = loritta.lorittaCluster

		val jsonObject = jsonObject(
				"id" to currentShard.id,
				"name" to currentShard.name,
				"globalRateLimitHits" to loritta.bucketedController?.getGlobalRateLimitHitsInTheLastMinute(),
				"isIgnoringRequests" to loritta.rateLimitChecker.checkIfRequestShouldBeIgnored(),
				"minShard" to currentShard.minShard,
				"maxShard" to currentShard.maxShard,
				"uptime" to ManagementFactory.getRuntimeMXBean().uptime
		)

		val array = jsonArray()

		for (shard in lorittaShards.shardManager.shards) {
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