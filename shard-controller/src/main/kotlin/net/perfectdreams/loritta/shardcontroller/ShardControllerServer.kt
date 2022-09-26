package net.perfectdreams.loritta.shardcontroller

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.Gson
import com.google.gson.JsonElement
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

class ShardControllerServer {
	companion object {
		val gson = Gson()
		val logger = KotlinLogging.logger {}
	}

	val mutex = Mutex()
	val loginPoolsLoggingInStatus = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.MINUTES)
			.build<Int, Boolean>()
			.asMap()

	fun start() {
		val server = embeddedServer(Netty, port = 6667) {
			routing {
				get("/api/v1/login-pools") {
					val userAgent = call.request.header("User-Agent")
					val wasLockedBefore = mutex.isLocked
					logger.info { "$userAgent requested all login pool statuses!" }

					mutex.withLock {
						val loginPoolStatuses = jsonObject()

						for (x in 0 until 16) {
							val status = loginPoolsLoggingInStatus.getOrDefault(x, false)
							loginPoolStatuses[x.toString()] = status
						}

						call.respondJson(
								jsonObject(
										"uptime" to ManagementFactory.getRuntimeMXBean().uptime,
										"isMutexLocked" to wasLockedBefore,
										"loginPools" to loginPoolStatuses
								)
						)
					}
				}

				get("/api/v1/shard/{shardId}") {
					val userAgent = call.request.header("User-Agent")
					val shardId = call.parameters["shardId"]!!.toInt()
					val loginPool = shardId.rem(16)

					logger.info { "$userAgent requested $shardId (login pool: $loginPool) status!" }

					mutex.withLock {
						val isLoggingIn = loginPoolsLoggingInStatus.getOrPut(loginPool, { false })

						call.respondJson(
								jsonObject(
										"allowLogin" to isLoggingIn
								)
						)
					}
				}

				put("/api/v1/shard/{shardId}") {
					val userAgent = call.request.header("User-Agent")
					val shardId = call.parameters["shardId"]!!.toInt()
					val loginPool = shardId.rem(16)

					logger.info { "$userAgent requested $shardId (login pool: $loginPool) to be set as being logged in! (if available)" }

					mutex.withLock {
						val isLoggingIn = loginPoolsLoggingInStatus.getOrPut(loginPool, { false })

						val success: Boolean
						if (!isLoggingIn) {
							loginPoolsLoggingInStatus[loginPool] = true
							success = true
							logger.info { "$userAgent set $shardId (login pool: $loginPool) for login! Gotta go fast!" }
						} else {
							success = false
							logger.info { "$userAgent requested $shardId (login pool: $loginPool) to be set as being logged in, but it is already being in use!" }
						}

						call.respondJson(
								jsonObject(),
								if (success)
									HttpStatusCode.OK
						else
									HttpStatusCode.Conflict
						)
					}
				}

				delete("/api/v1/shard/{shardId}") {
					val userAgent = call.request.header("User-Agent")
					val shardId = call.parameters["shardId"]!!.toInt()
					val loginPool = shardId.rem(16)

					logger.info { "$userAgent requested $shardId (login pool: $loginPool) to be set as already logged in!" }

					mutex.withLock {
						loginPoolsLoggingInStatus.remove(loginPool)

						call.respondJson(
								jsonObject()
						)
					}
				}
			}
		}
		server.start(wait = true)
	}

	private suspend fun ApplicationCall.respondJson(response: JsonElement, httpStatusCode: HttpStatusCode = HttpStatusCode.OK) = this.respondText(ContentType.Application.Json, httpStatusCode, { gson.toJson(response) })
}
