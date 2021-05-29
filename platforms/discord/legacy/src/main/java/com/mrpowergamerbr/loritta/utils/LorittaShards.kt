package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.common.cache.CacheBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.sharding.ShardManager
import net.perfectdreams.loritta.tables.CachedDiscordUsers
import net.perfectdreams.loritta.utils.CachedUserInfo
import net.perfectdreams.loritta.utils.ClusterNotReadyException
import net.perfectdreams.loritta.utils.ClusterOfflineException
import net.perfectdreams.loritta.utils.DiscordUtils
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Guarda todos as shards da Loritta
 */
class LorittaShards {
	companion object {
		internal val logger = KotlinLogging.logger {}
	}
	lateinit var shardManager: ShardManager
	val cachedRetrievedUsers = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES)
			.build<Long, Optional<User>>()

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

	suspend fun retrieveUserById(id: String?) = retrieveUserById(id?.toLongOrNull())

	suspend fun retrieveUserInfoById(id: Long?): CachedUserInfo? {
		if (id == null)
			return null

		// Ao dar retrieve na info do user, primeiro iremos tentar verificar se a gente tem ele no user cache do JDA
		val userInJdaCache = lorittaShards.getUserById(id)
		if (userInJdaCache != null)
			return transformUserToCachedUserInfo(userInJdaCache)

		// Se não tiver, vamos verificar no cache local de retrieved users
		val cachedRetrievedUser = cachedRetrievedUsers.getIfPresent(id)
		if (cachedRetrievedUser != null)
			return transformUserToCachedUserInfo(cachedRetrievedUser.get())

		// Se não tiver, iremos verificar na database externa
		val cachedUser = loritta.newSuspendedTransaction {
			CachedDiscordUsers.select { CachedDiscordUsers.id eq id }
					.firstOrNull()
		}

		if (cachedUser != null)
			return CachedUserInfo(
					cachedUser[CachedDiscordUsers.id].value,
					cachedUser[CachedDiscordUsers.name],
					cachedUser[CachedDiscordUsers.discriminator],
					cachedUser[CachedDiscordUsers.avatarId]
			)

		// E se *ainda* não tiver, iremos dar retrieve
		val discordUser = retrieveUserById(id)

		return if (discordUser != null) {
			transformUserToCachedUserInfo(discordUser)
		} else null
	}

	suspend fun retrieveUserInfoByTag(username: String, discriminator: String): CachedUserInfo? {
		// When retrieving the user's info via tag, we will search in JDA's user cache
		val userInJdaCache = lorittaShards.shardManager.getUserByTag(username, discriminator)
		if (userInJdaCache != null)
			return transformUserToCachedUserInfo(userInJdaCache)

		// If not, we will check on the local cache of retrieved users
		val cachedRetrievedUser = cachedRetrievedUsers.asMap().values
				.asSequence()
				.filter { it.isPresent }
				.map { it.get() }
				.filter { it.name == username && it.discriminator == discriminator }
				.firstOrNull()

		if (cachedRetrievedUser != null)
			return transformUserToCachedUserInfo(cachedRetrievedUser)

		// If it doesn't exist, check on the external database
		val cachedUser = loritta.newSuspendedTransaction {
			CachedDiscordUsers.select { CachedDiscordUsers.name eq username and (CachedDiscordUsers.discriminator eq discriminator) }
					.firstOrNull()
		}

		if (cachedUser != null)
			return CachedUserInfo(
					cachedUser[CachedDiscordUsers.id].value,
					cachedUser[CachedDiscordUsers.name],
					cachedUser[CachedDiscordUsers.discriminator],
					cachedUser[CachedDiscordUsers.avatarId]
			)

		// And if it doesn't exist... oh well, let's try finding it in another cluster
		val results = searchUserInAllLorittaClusters(username, discriminator, limit = 1)
		val result = results.firstOrNull()
		if (result != null) {
			updateCachedUserData(result["id"].long, result["name"].string, result["discriminator"].string, result["avatarId"].nullString)
			return CachedUserInfo(
					result["id"].long,
					result["name"].string,
					result["discriminator"].string,
					result["avatarId"].nullString
			)
		}

		return null
	}

	@Suppress("IMPLICIT_CAST_TO_ANY")
	suspend fun retrieveUserById(id: Long?): User? {
		if (id == null)
			return null

		val cachedUser = cachedRetrievedUsers.getIfPresent(id)
		if (cachedUser != null)
			return cachedUser.getOrNull()

		val user = shardManager.retrieveUserById(id).await()
		cachedRetrievedUsers.put(id, Optional.of(user))

		if (user != null)
			updateCachedUserData(user)

		return user
	}

	suspend fun updateCachedUserData(user: User) = updateCachedUserData(user.idLong, user.name, user.discriminator, user.avatarId)

	@Suppress("IMPLICIT_CAST_TO_ANY")
	suspend fun updateCachedUserData(id: Long, name: String, discriminator: String, avatarId: String?) {
		val now = System.currentTimeMillis()
		loritta.newSuspendedTransaction {
			val cachedData = CachedDiscordUsers.select { CachedDiscordUsers.id eq id }.firstOrNull()

			if (cachedData != null) {
				CachedDiscordUsers.update({ CachedDiscordUsers.id eq id }) {
					it[CachedDiscordUsers.name] = name
					it[CachedDiscordUsers.discriminator] = discriminator
					it[CachedDiscordUsers.avatarId] = avatarId
					it[CachedDiscordUsers.updatedAt] = now
				}
			} else {
				CachedDiscordUsers.insert {
					it[CachedDiscordUsers.id] = EntityID(id, CachedDiscordUsers)
					it[CachedDiscordUsers.name] = name
					it[CachedDiscordUsers.discriminator] = discriminator
					it[CachedDiscordUsers.avatarId] = avatarId
					it[createdAt] = now
					it[updatedAt] = now
				}
			}
		}
	}

	private fun transformUserToCachedUserInfo(user: User) = CachedUserInfo(
			user.idLong,
			user.name,
			user.discriminator,
			user.avatarId
	)

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

		return GlobalScope.async(loritta.coroutineDispatcher) {
			try {
				val body = withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
					val response = loritta.http.get<HttpResponse>("https://${shard.getUrl()}$path") {
						header("Authorization", loritta.lorittaInternalApiKey.name)
						userAgent(loritta.lorittaCluster.getUserAgent())
					}

					response.readText()
				}

				JsonParser.parseString(
						body
				)
			} catch (e: Exception) {
				logger.warn(e) { "Shard ${shard.name} ${shard.id} offline!" }
				throw ClusterOfflineException(shard.id, shard.name)
			}
		}
	}

	fun queryCluster(cluster: GeneralConfig.LorittaClusterConfig, path: String): Deferred<JsonElement> {
		return GlobalScope.async(loritta.coroutineDispatcher) {
			try {
				val body = withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
					val response = loritta.http.get<HttpResponse>("https://${cluster.getUrl()}$path") {
						header("Authorization", loritta.lorittaInternalApiKey.name)
						userAgent(loritta.lorittaCluster.getUserAgent())
					}

					response.readText()
				}

				JsonParser.parseString(
						body
				)
			} catch (e: Exception) {
				logger.warn(e) { "Shard ${cluster.name} ${cluster.id} offline!" }
				throw ClusterOfflineException(cluster.id, cluster.name)
			}
		}
	}

	fun queryAllLorittaClusters(path: String): List<Deferred<JsonElement>> {
		val shards = loritta.config.clusters

		return shards.map {
			GlobalScope.async(loritta.coroutineDispatcher) {
				try {
					withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
						logger.info { "Executing ${path} to ${it.getUserAgent()}" }

						val response = loritta.http.get<HttpResponse>("https://${it.getUrl()}$path") {
							userAgent(loritta.lorittaCluster.getUserAgent())
							header("Authorization", loritta.lorittaInternalApiKey.name)
						}
						logger.info { "Successfully got a response from ${it.getUserAgent()} for $path" }

						val body = response.readText()
						JsonParser.parseString(
								body
						)
					}
				} catch (e: Exception) {
					logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
					throw ClusterOfflineException(it.id, it.name)
				}
			}
		}
	}

	suspend fun queryMutualGuildsInAllLorittaClusters(userId: String): List<JsonObject> {
		val results = queryAllLorittaClusters("/api/v1/users/$userId/mutual-guilds")

		val allGuilds = mutableListOf<JsonObject>()

		results.forEach {
			try {
				val json = it.await()

				json["guilds"].array.forEach {
					allGuilds.add(it.obj)
				}
			} catch (e: ClusterOfflineException) {}
		}

		return allGuilds
	}

	suspend fun searchUserInAllLorittaClusters(username: String, discriminator: String? = null, isRegExPattern: Boolean = false, limit: Int? = null): List<JsonObject> {
		val shards = loritta.config.clusters

		val results = shards.map {
			GlobalScope.async(loritta.coroutineDispatcher) {
				try {
					withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
						val response = loritta.http.post<HttpResponse>("https://${it.getUrl()}/api/v1/users/search") {
							header("Authorization", loritta.lorittaInternalApiKey.name)
							userAgent(loritta.lorittaCluster.getUserAgent())

							body = gson.toJson(
									jsonObject(
											"isRegExPattern" to isRegExPattern,
											"limit" to limit,
											"username" to username,
											"discriminator" to discriminator
									)
							)
						}

						val body = response.readText()
						JsonParser.parseString(
								body
						)
					}
				} catch (e: Exception) {
					logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
					throw ClusterOfflineException(it.id, it.name)
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
			} catch (e: ClusterOfflineException) {}
		}

		return matchedUsers.distinctBy { it["id"].long }
	}

	suspend fun searchGuildInAllLorittaClusters(pattern: String): List<JsonObject> {
		val shards = loritta.config.clusters

		val results = shards.map {
			GlobalScope.async(loritta.coroutineDispatcher) {
				try {
					withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
						val response = loritta.http.post<HttpResponse>("https://${it.getUrl()}/api/v1/guilds/search") {
							header("Authorization", loritta.lorittaInternalApiKey.name)
							userAgent(loritta.lorittaCluster.getUserAgent())

							body = gson.toJson(
									jsonObject("pattern" to pattern)
							)
						}

						val body = response.readText()
						JsonParser.parseString(
								body
						)
					}
				} catch (e: Exception) {
					logger.warn(e) { "Shard ${it.name} ${it.id} offline!" }
					throw ClusterOfflineException(it.id, it.name)
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
			} catch (e: ClusterOfflineException) {}
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

	/**
	 * Queries the current guild count but, if any of the clusters are not ready (offline or not CONNECTED), an exception will be thrown.
	 *
	 * @return the guild count
	 * @throws ClusterOfflineException if a cluster is offline
	 * @throws ClusterNotReadyException if a cluster is not ready (not all shards are CONNECTED)
	 */
	suspend fun queryGuildCountOrThrowExceptionIfAnyClusterIsNotReady(): Int {
		var guildCount = 0

		val results = lorittaShards.queryAllLorittaClusters("/api/v1/loritta/status")
		results.forEach {
			try {
				val json = it.await()

				val shardsArray = json["shards"].array
				val anyNotReady = shardsArray.any { it["status"].string != "CONNECTED" }

				if (anyNotReady)
					throw ClusterNotReadyException(json["id"].long, json["name"].string)

				guildCount += json["shards"].array.sumBy { it["guildCount"].int }
			} catch (e: Exception) {
				throw e
			}
		}
		return guildCount
	}

	suspend fun queryGuildById(id: String) = queryGuildById(id.toLong())

	suspend fun queryGuildById(id: Long): JsonObject? {
		val shardId = DiscordUtils.getShardIdFromGuildId(id)
		val clusterId = DiscordUtils.getLorittaClusterIdForShardId(shardId)
		val url = DiscordUtils.getUrlForLorittaClusterId(clusterId)

		val body = withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
			val response = loritta.http.get<HttpResponse>("https://$url/api/v1/guilds/$id") {
				header("Authorization", loritta.lorittaInternalApiKey.name)
				userAgent(loritta.lorittaCluster.getUserAgent())
			}

			response.readText()
		}

		val json = JsonParser.parseString(body).obj
		if (!json.has("id"))
			return null

		return json
	}
}