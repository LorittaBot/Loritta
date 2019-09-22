package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path
import java.lang.management.ManagementFactory

@Path("/api/v1/loritta/status")
class GetStatusController {
	@GET
	@LoriDoNotLocaleRedirect(true)
	fun handle(req: Request, res: Response) {
		res.type(MediaType.json)

		val currentShard = loritta.lorittaCluster

		val jsonObject = jsonObject(
				"id" to currentShard.id,
				"name" to currentShard.name,
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

		res.send(jsonObject)
	}
}