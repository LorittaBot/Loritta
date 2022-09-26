package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

import dev.kord.common.entity.DiscordConnection
import dev.kord.common.entity.DiscordPartialGuild
import dev.kord.common.entity.DiscordUser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.utils.JsonIgnoreUnknownKeys

class TemmieDiscordAuth(val clientId: String,
                        val clientSecret: String,
                        val authCode: String?,
                        val redirectUri: String,
                        val scope: List<String>,
                        var accessToken: String? = null,
                        var refreshToken: String? = null,
                        var expiresIn: Long? = null,
                        var generatedAt: Long? = null
) {
    companion object {
        private const val PREFIX = "https://discordapp.com/api"
        private const val USER_IDENTIFICATION_URL = "$PREFIX/users/@me"
        private const val CONNECTIONS_URL = "$USER_IDENTIFICATION_URL/connections"
        private const val USER_GUILDS_URL = "$USER_IDENTIFICATION_URL/guilds"
        private const val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
        private const val USER_AGENT = "Loritta-Morenitta-Discord-Auth/1.0"
        private val logger = KotlinLogging.logger {}
        val http = HttpClient {
            this.expectSuccess = false
        }
    }

    private val mutex = Mutex()

    suspend fun doTokenExchange(): JsonObject {
        logger.info { "doTokenExchange()" }
        val authCode = authCode ?: throw RuntimeException("Trying to do token exchange without authCode!")

        val parameters = Parameters.build {
            append("client_id", clientId)
            append("client_secret", clientSecret)
            append("grant_type", "authorization_code")
            append("code", authCode)
            append("redirect_uri", redirectUri)
            // append("scope", scope.joinToString(" "))
        }

        return doStuff {
            val result = http.post {
                url(TOKEN_BASE_URL)
                userAgent(USER_AGENT)

                setBody(
                    TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
                )
            }.bodyAsText()

            logger.info { result }

            val tree = Json.parseToJsonElement(result)
                .jsonObject

            if (tree.containsKey("error"))
                throw TokenExchangeException("Error while exchanging token: ${tree["error"]}")

            readTokenPayload(tree)

            tree
        }
    }

    suspend fun refreshToken() {
        logger.info { "refreshToken()" }
        val refreshToken = refreshToken ?: throw RuntimeException()

        val parameters = Parameters.build {
            append("client_id", clientId)
            append("client_secret", clientSecret)
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
            append("redirect_uri", redirectUri)
            // append("scope", scope.joinToString(" "))
        }

        doStuff(false) {
            val result = checkIfRequestWasValid(
                http.post {
                    url(TOKEN_BASE_URL)
                    userAgent(USER_AGENT)

                    setBody(
                        TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded)
                    )
                }
            )

            logger.info { result }

            val tree = Json.parseToJsonElement(result)
                .jsonObject

            if (tree.containsKey("error"))
                throw TokenExchangeException("Error while exchanging token: ${tree["error"]}")

            val resultAsJson = Json.parseToJsonElement(result)
                .jsonObject

            checkForRateLimit(resultAsJson)
            readTokenPayload(resultAsJson)
        }
    }

    suspend fun getUserIdentification(): DiscordUser {
        logger.info { "getUserIdentification()" }
        return doStuff {
            val result = checkIfRequestWasValid(
                http.get {
                    url(USER_IDENTIFICATION_URL)
                    userAgent(USER_AGENT)
                    header("Authorization", "Bearer $accessToken")
                }
            )

            logger.info { result }

            val resultAsJson = Json.parseToJsonElement(result)
            checkForRateLimit(resultAsJson)

            return@doStuff JsonIgnoreUnknownKeys.decodeFromJsonElement(resultAsJson)
        }
    }

    suspend fun getUserGuilds(): List<DiscordPartialGuild> {
        logger.info { "getUserGuilds()" }
        return doStuff {
            val result = checkIfRequestWasValid(
                http.get {
                    url(USER_GUILDS_URL)
                    userAgent(USER_AGENT)
                    header("Authorization", "Bearer $accessToken")
                }
            )

            logger.info { result }

            val resultAsJson = Json.parseToJsonElement(result)
            checkForRateLimit(resultAsJson)

            return@doStuff JsonIgnoreUnknownKeys.decodeFromJsonElement(resultAsJson)
        }
    }

    suspend fun getUserConnections(): List<DiscordConnection> {
        logger.info { "getUserConnections()" }
        return doStuff {
            val result = checkIfRequestWasValid(
                http.get {
                    url(CONNECTIONS_URL)
                    userAgent(USER_AGENT)
                    header("Authorization", "Bearer $accessToken")
                }
            )

            logger.info { result }

            val resultAsJson = Json.parseToJsonElement(result)
            checkForRateLimit(resultAsJson)

            return@doStuff JsonIgnoreUnknownKeys.decodeFromJsonElement(resultAsJson)
        }
    }

    private suspend fun refreshTokenIfNeeded() {
        logger.info { "refreshTokenIfNeeded()" }
        val generatedAt = generatedAt
        val expiresIn = expiresIn

        if (generatedAt != null && expiresIn != null) {
            if (System.currentTimeMillis() >= generatedAt + (expiresIn * 1000))
                throw NeedsRefreshException()
        }

        return
    }

    private suspend fun <T> doStuff(checkForRefresh: Boolean = true, callback: suspend () -> (T)): T {
        logger.info { "doStuff(...) mutex locked? ${mutex.isLocked}" }
        return try {
            if (checkForRefresh)
                refreshTokenIfNeeded()

            mutex.withLock {
                callback.invoke()
            }
        } catch (e: RateLimitedException) {
            logger.info { "rate limited exception! locked? ${mutex.isLocked}" }
            return doStuff(checkForRefresh, callback)
        } catch (e: NeedsRefreshException) {
            logger.info { "refresh exception!" }
            refreshToken()
            doStuff(checkForRefresh, callback)
        }
    }

    private fun readTokenPayload(payload: JsonObject) {
        accessToken = payload["access_token"]?.jsonPrimitive?.contentOrNull
        refreshToken = payload["refresh_token"]?.jsonPrimitive?.contentOrNull
        expiresIn = payload["expires_in"]?.jsonPrimitive?.longOrNull
        generatedAt = System.currentTimeMillis()
    }

    private suspend fun checkForRateLimit(element: JsonElement): Boolean {
        if (element is JsonObject) {
            val asObject = element.jsonObject
            val retryAfter = asObject["retry_after"]?.jsonPrimitive?.long

            if (retryAfter != null) {
                logger.info { "Got rate limited, oof! Retry After: $retryAfter" }
                // oof, ratelimited!
                delay(retryAfter)
                throw RateLimitedException()
            }
        }

        return false
    }

    private suspend fun checkIfRequestWasValid(response: HttpResponse): String {
        if (response.status.value == 401)
            throw TokenUnauthorizedException(response.status)

        return response.bodyAsText()
    }

    class TokenUnauthorizedException(val status: HttpStatusCode) : RuntimeException()
    class TokenExchangeException(message: String) : RuntimeException(message)

    private class RateLimitedException : RuntimeException()
    private class NeedsRefreshException : RuntimeException()
}