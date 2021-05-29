package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.lang.management.ManagementFactory
import java.util.jar.Attributes
import java.util.jar.JarFile

class GetStatusRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/loritta/status") {
	override suspend fun onRequest(call: ApplicationCall) {
		loritta as Loritta
		val currentShard = loritta.lorittaCluster

		val path = this::class.java.protectionDomain.codeSource.location.path
		val jar = JarFile(path)
		val mf = jar.manifest
		val mattr = mf.mainAttributes

		val lorittaVersion = mattr[Attributes.Name("Loritta-Version")] as String
		val buildNumber = mattr[Attributes.Name("Build-Number")] as String
		val commitHash = mattr[Attributes.Name("Commit-Hash")] as String
		val gitBranch = mattr[Attributes.Name("Git-Branch")] as String
		val compiledAt = mattr[Attributes.Name("Compiled-At")] as String
		val kotlinVersion = mattr[Attributes.Name("Kotlin-Version")] as String
		val jdaVersion = mattr[Attributes.Name("JDA-Version")] as String

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
						"kotlin" to kotlinVersion,
						"java" to System.getProperty("java.version"),
						"jda" to jdaVersion
				),
				"build" to jsonObject(
						"version" to lorittaVersion,
						"buildNumber" to buildNumber,
						"commitHash" to commitHash,
						"gitBranch" to gitBranch,
						"compiledAt" to compiledAt,
						"environment" to com.mrpowergamerbr.loritta.utils.loritta.config.loritta.environment.name
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