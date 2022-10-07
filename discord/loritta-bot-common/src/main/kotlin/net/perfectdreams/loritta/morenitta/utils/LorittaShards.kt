package net.perfectdreams.loritta.morenitta.utils

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
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.rest.request.KtorRequestException
import net.perfectdreams.loritta.morenitta.utils.extensions.getOrNull
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.entities.Emote
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.tables.CachedDiscordUsers
import net.perfectdreams.loritta.morenitta.utils.config.LorittaConfig
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
class LorittaShards(val loritta: LorittaBot) {
	companion object {
		internal val logger = KotlinLogging.logger {}
	}
	val cachedRetrievedUsers = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES)
		.build<Long, Optional<User>>()

	suspend fun getGuildById(id: String): Guild? = loritta.deviousFun.getGuildById(id)
	suspend fun getGuildById(id: Long): Guild? = loritta.deviousFun.getGuildById(id)

	suspend fun getGuildCount(): Long = loritta.deviousFun.getGuildCount()

	suspend fun getUserById(id: String?) = getUserById(id?.toLong())

	suspend fun getUserById(id: Long?): User? {
		if (id == null)
			return null

		return loritta.deviousFun.getUserById(Snowflake(id))
	}

	suspend fun retrieveUserById(id: String?) = retrieveUserById(id?.toLongOrNull())

	suspend fun retrieveUserInfoById(id: Long?): CachedUserInfo? {
		if (id == null)
			return null

		// Ao dar retrieve na info do user, primeiro iremos tentar verificar se a gente tem ele no user cache do JDA
		val userInJdaCache = loritta.lorittaShards.getUserById(id)
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

	@Suppress("IMPLICIT_CAST_TO_ANY")
	suspend fun retrieveUserById(id: Long?): User? {
		if (id == null)
			return null

		val cachedUser = cachedRetrievedUsers.getIfPresent(id)
		if (cachedUser != null)
			return cachedUser.getOrNull()

		val user = loritta.deviousFun.retrieveUserOrNullById(Snowflake(id))

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

	suspend fun getTextChannelById(id: String?) = getTextChannelById(id?.toLong())

	suspend fun getTextChannelById(id: Long?): Channel? {
		if (id == null)
			return null

		val cachedChannel = loritta.deviousFun.getChannelById(id) ?: return null
		if (cachedChannel.type != ChannelType.GuildText)
			return null

		return cachedChannel
	}

	fun getShards(): List<DeviousGateway> = loritta.deviousFun.gatewayManager.gateways.values.toList()

	fun queryMasterLorittaCluster(path: String): Deferred<JsonElement> {
		val shard = loritta.config.loritta.clusters.instances.first { it.id == 1 }

		return GlobalScope.async(loritta.coroutineDispatcher) {
			try {
				val body = withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
					val response = loritta.http.get("https://${shard.getUrl(loritta)}$path") {
						header("Authorization", loritta.lorittaInternalApiKey.name)
						userAgent(loritta.lorittaCluster.getUserAgent(loritta))
					}

					response.bodyAsText()
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

	fun queryCluster(cluster: LorittaConfig.LorittaClustersConfig.LorittaClusterConfig, path: String): Deferred<JsonElement> {
		return GlobalScope.async(loritta.coroutineDispatcher) {
			try {
				val body = withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
					val response = loritta.http.get("https://${cluster.getUrl(loritta)}$path") {
						header("Authorization", loritta.lorittaInternalApiKey.name)
						userAgent(loritta.lorittaCluster.getUserAgent(loritta))
					}

					response.bodyAsText()
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
		val shards = loritta.config.loritta.clusters.instances

		return shards.map {
			GlobalScope.async(loritta.coroutineDispatcher) {
				try {
					withTimeout(loritta.config.loritta.clusterConnectionTimeout.toLong()) {
						logger.info { "Executing ${path} to ${it.getUserAgent(loritta)}" }

						val response = loritta.http.get("https://${it.getUrl(loritta)}$path") {
							userAgent(loritta.lorittaCluster.getUserAgent(loritta))
							header("Authorization", loritta.lorittaInternalApiKey.name)
						}
						logger.info { "Successfully got a response from ${it.getUserAgent(loritta)} for $path" }

						val body = response.bodyAsText()
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

	/**
	 * Queries the current guild count but, if any of the clusters are not ready (offline or not CONNECTED), an exception will be thrown.
	 *
	 * @return the guild count
	 * @throws ClusterOfflineException if a cluster is offline
	 * @throws ClusterNotReadyException if a cluster is not ready (not all shards are CONNECTED)
	 */
	suspend fun queryGuildCountOrThrowExceptionIfAnyClusterIsNotReady(): Int {
		var guildCount = 0

		val results = loritta.lorittaShards.queryAllLorittaClusters("/api/v1/loritta/status")
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
}