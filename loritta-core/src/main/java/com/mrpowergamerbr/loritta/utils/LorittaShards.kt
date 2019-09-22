package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.vanilla.misc.PingCommand
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.extensions.await
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.sharding.ShardManager
import net.perfectdreams.loritta.utils.DiscordUtils

/**
 * Guarda todos as shards da Loritta
 */
class LorittaShards {
	companion object {
		internal val logger = KotlinLogging.logger {}
	}
	lateinit var shardManager: ShardManager

	fun getGuildById(id: String): Guild? = shardManager.getGuildById(id)
	fun getGuildById(id: Long): Guild? = shardManager.getGuildById(id)

	fun getGuilds(): List<Guild> = shardManager.guilds

	fun getGuildCount(): Int = shardManager.guilds.size

	fun getCachedGuildCount(): Long = shardManager.guildCache.size()

	fun getUserCount(): Int = shardManager.users.size

	fun getCachedUserCount(): Long = shardManager.userCache.size()

	fun getEmoteCount(): Int = shardManager.emotes.size

	fun getCachedEmoteCount(): Long = shardManager.emoteCache.size()

	fun getChannelCount(): Int = shardManager.textChannels.size + shardManager.voiceChannels.size

	fun getCachedChannelCount(): Long = shardManager.textChannelCache.size() + shardManager.voiceChannels.size

	fun getTextChannelCount(): Int = shardManager.textChannels.size

	fun getCachedTextChannelCount(): Long = shardManager.textChannelCache.size()

	fun getVoiceChannelCount(): Int  = shardManager.voiceChannels.size

	fun getCachedVoiceChannelCount(): Long = shardManager.voiceChannelCache.size()

	fun getUsers(): List<User> = shardManager.users

	fun getUserById(id: String?): User? {
		if (id == null)
			return null

		return shardManager.getUserById(id)
	}

	fun getUserById(id: Long?): User? {
		if (id == null)
			return null

		return shardManager.getUserById(id)
	}

	suspend fun retrieveUserById(id: String?): User? {
		if (id == null)
			return null

		return getUserById(id) ?: shardManager.retrieveUserById(id).await()
	}

	suspend fun retrieveUserById(id: Long?): User? {
		if (id == null)
			return null

		return getUserById(id) ?: shardManager.retrieveUserById(id).await()
	}

	fun getMutualGuilds(user: User): List<Guild> = shardManager.getMutualGuilds(user)

	fun getEmoteById(id: String?): Emote? {
		if (id == null)
			return null

		return shardManager.getEmoteById(id)
	}

	fun getTextChannelById(id: String?): TextChannel? {
		if (id == null)
			return null

		return shardManager.getTextChannelById(id)
	}

	fun getShards(): List<JDA> {
		return shardManager.shards
	}

	fun queryMasterLorittaCluster(path: String): Deferred<JsonElement> {
		val shard = loritta.config.clusters.first { it.id == 1L }

		return GlobalScope.async {
			try {
				val body = HttpRequest.get("https://${shard.getUrl()}$path")
						.userAgent(loritta.lorittaCluster.getUserAgent())
						.header("Authorization", loritta.lorittaInternalApiKey.name)
						.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
						.readTimeout(loritta.config.loritta.clusterReadTimeout)
						.body()

				jsonParser.parse(
						body
				)
			} catch (e: Exception) {
				logger.warn(e) { "Shard ${shard.name} ${shard.id} offline!" }
				throw PingCommand.ShardOfflineException(shard.id, shard.name)
			}
		}
	}

	fun queryCluster(cluster: GeneralConfig.LorittaClusterConfig, path: String): Deferred<JsonElement> {
		return GlobalScope.async {
			try {
				val body = HttpRequest.get("https://${cluster.getUrl()}$path")
						.userAgent(loritta.lorittaCluster.getUserAgent())
						.header("Authorization", loritta.lorittaInternalApiKey.name)
						.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
						.readTimeout(loritta.config.loritta.clusterReadTimeout)
						.body()

				jsonParser.parse(
						body
				)
			} catch (e: Exception) {
				logger.warn(e) { "Shard ${cluster.name} ${cluster.id} offline!" }
				throw PingCommand.ShardOfflineException(cluster.id, cluster.name)
			}
		}
	}

	fun queryAllLorittaClusters(path: String): List<Deferred<JsonElement>> {
		val shards = loritta.config.clusters

		return shards.map {
			GlobalScope.async {
				try {
					val body = HttpRequest.get("https://${it.getUrl()}$path")
							.userAgent(loritta.lorittaCluster.getUserAgent())
							.header("Authorization", loritta.lorittaInternalApiKey.name)
							.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
							.readTimeout(loritta.config.loritta.clusterReadTimeout)
							.body()

					jsonParser.parse(
							body
					)
				} catch (e: Exception) {
					logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
					throw PingCommand.ShardOfflineException(it.id, it.name)
				}
			}
		}
	}

	suspend fun queryMutualGuildsInAllLorittaClusters(userId: String): List<JsonObject> {
		val results = queryAllLorittaClusters("/api/v1/loritta/user/$userId/mutual-guilds")

		val allGuilds = mutableListOf<JsonObject>()

		results.forEach {
			try {
				val json = it.await()

				json["guilds"].array.forEach {
					allGuilds.add(it.obj)
				}
			} catch (e: PingCommand.ShardOfflineException) {}
		}

		return allGuilds
	}

	suspend fun searchUserInAllLorittaClusters(pattern: String): List<JsonObject> {
		val shards = loritta.config.clusters

		val results = shards.map {
			GlobalScope.async {
				try {
					val body = HttpRequest.post("https://${it.getUrl()}/api/v1/loritta/user/search")
							.userAgent(loritta.lorittaCluster.getUserAgent())
							.header("Authorization", loritta.lorittaInternalApiKey.name)
							.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
							.readTimeout(loritta.config.loritta.clusterReadTimeout)
							.send(
									gson.toJson(
											jsonObject("pattern" to pattern)
									)
							)
							.body()

					jsonParser.parse(
							body
					)
				} catch (e: Exception) {
					logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
					throw PingCommand.ShardOfflineException(it.id, it.name)
				}
			}
		}

		val matchedUsers = mutableListOf<JsonObject>()

		results.forEach {
			try {
				val json = it.await()

				json.array.forEach {
					matchedUsers.add(it.obj)
				}
			} catch (e: PingCommand.ShardOfflineException) {}
		}

		return matchedUsers.distinctBy { it["id"].long }
	}

	suspend fun searchGuildInAllLorittaClusters(pattern: String): List<JsonObject> {
		val shards = loritta.config.clusters

		val results = shards.map {
			GlobalScope.async {
				try {
					val body = HttpRequest.post("https://${it.getUrl()}/api/v1/loritta/guild/search")
							.userAgent(loritta.lorittaCluster.getUserAgent())
							.header("Authorization", loritta.lorittaInternalApiKey.name)
							.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
							.readTimeout(loritta.config.loritta.clusterReadTimeout)
							.send(
									gson.toJson(
											jsonObject("pattern" to pattern)
									)
							)
							.body()

					jsonParser.parse(
							body
					)
				} catch (e: Exception) {
					logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
					throw PingCommand.ShardOfflineException(it.id, it.name)
				}
			}
		}

		val matchedGuilds = mutableListOf<JsonObject>()

		results.forEach {
			try {
				val json = it.await()

				json.array.forEach {
					matchedGuilds.add(it.obj)
				}
			} catch (e: PingCommand.ShardOfflineException) {}
		}

		return matchedGuilds
	}

	suspend fun queryGuildCount(): Int {
		var guildCount = 0

		val results = lorittaShards.queryAllLorittaClusters("/api/v1/loritta/status")
		results.forEach {
			try {
				val json = it.await()

				guildCount += json["shards"].array.sumBy { it["guildCount"].int }
			} catch (e: Exception) {}
		}
		return guildCount
	}

	suspend fun queryGuildById(id: String) = queryGuildById(id.toLong())

	suspend fun queryGuildById(id: Long): JsonObject? {
		val shardId = DiscordUtils.getShardIdFromGuildId(id)
		val clusterId = DiscordUtils.getLorittaClusterIdForShardId(shardId)
		val url = DiscordUtils.getUrlForLorittaClusterId(clusterId)

		val body = HttpRequest.get("https://$url/api/v1/loritta/guild/$id")
				.userAgent(loritta.lorittaCluster.getUserAgent())
				.header("Authorization", loritta.lorittaInternalApiKey.name)
				.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
				.readTimeout(loritta.config.loritta.clusterReadTimeout)
				.body()

		val json = jsonParser.parse(body).obj
		if (!json.has("id"))
			return null

		return json
	}
}