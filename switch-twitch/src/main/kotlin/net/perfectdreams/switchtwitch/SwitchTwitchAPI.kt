package net.perfectdreams.switchtwitch

import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import mu.KotlinLogging
import net.perfectdreams.switchtwitch.data.*

class SwitchTwitchAPI(
    val clientId: String,
    val clientSecret: String,
    var accessToken: String? = null,
    // https://discuss.dev.twitch.tv/t/id-token-missing-when-using-id-twitch-tv-oauth2-token-with-grant-type-refresh-token/18263/3
    // var refreshToken: String? = null,
    var expiresIn: Long? = null,
    var generatedAt: Long? = null
) {
    companion object {
        private val JsonIgnoreUnknownKeys = Json { ignoreUnknownKeys = true }
        private const val PREFIX = "https://id.twitch.tv"
        private const val API_PREFIX = "https://api.twitch.tv"
        private const val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
        private const val USER_AGENT = "SocialRelayer-Loritta-Morenitta-Twitch-Auth/1.0"
        private val logger = KotlinLogging.logger {}
        val http = HttpClient(CIO) {
            this.expectSuccess = false
        }

        suspend fun fromAuthCode(clientId: String, clientSecret: String, authCode: String, redirectUri: String): SwitchTwitchAPI {
            val parameters = Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("code", authCode)
                append("grant_type", "authorization_code")
                append("redirect_uri", redirectUri)
            }

            val start = System.currentTimeMillis()
            val response = http.post("https://id.twitch.tv/oauth2/token") {
                userAgent(USER_AGENT)
                setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
            }

            val json = Json.parseToJsonElement(response.bodyAsText())
                .jsonObject
            val accessToken = json["access_token"]!!.jsonPrimitive.content
            val refreshToken = json["refresh_token"]!!.jsonPrimitive.content
            val expiresIn = json["expires_in"]!!.jsonPrimitive.long

            return SwitchTwitchAPI(
                clientId,
                clientSecret,
                accessToken,
                // refreshToken,
                expiresIn,
                start
            )
        }
    }

    private val mutex = Mutex()

    suspend fun doTokenExchange(): JsonObject {
        logger.info { "doTokenExchange()" }

        val parameters = Parameters.build {
            append("client_id", clientId)
            append("client_secret", clientSecret)
            append("grant_type", "client_credentials")
        }

        return doStuff(checkForRefresh = false) {
            val result = http.post {
                url(TOKEN_BASE_URL)
                userAgent(USER_AGENT)

                setBody(TextContent(parameters.formUrlEncode(), ContentType.Application.FormUrlEncoded))
            }.bodyAsText()

            logger.info { result }

            val tree = JsonParser.parseString(result).asJsonObject

            if (tree.has("error"))
                throw TokenExchangeException("Error while exchanging token: ${tree["error"].asString}")

            readTokenPayload(tree)

            tree
        }
    }

    suspend fun refreshToken() {
        logger.info { "refreshToken()" }
        // https://discuss.dev.twitch.tv/t/id-token-missing-when-using-id-twitch-tv-oauth2-token-with-grant-type-refresh-token/18263/3
        doTokenExchange()
    }

    private suspend fun refreshTokenIfNeeded() {
        logger.info { "refreshTokenIfNeeded()" }
        if (accessToken == null)
            throw NeedsRefreshException()

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
            logger.info { "Rate limited exception! locked? ${mutex.isLocked}" }
            doStuff(checkForRefresh, callback)
        } catch (e: NeedsRefreshException) {
            logger.info { "Refresh exception!" }
            refreshToken()
            doStuff(checkForRefresh, callback)
        } catch (e: TokenUnauthorizedException) {
            logger.info { "Unauthorized token exception! Doing token exchange again and retrying..." }
            doTokenExchange()
            doStuff(checkForRefresh, callback)
        }
    }

    private fun readTokenPayload(payload: JsonObject) {
        accessToken = payload["access_token"].string
        // https://discuss.dev.twitch.tv/t/id-token-missing-when-using-id-twitch-tv-oauth2-token-with-grant-type-refresh-token/18263/3
        // refreshToken = payload["refresh_token"].string
        expiresIn = payload["expires_in"].long
        generatedAt = System.currentTimeMillis()
    }

    private suspend fun checkForRateLimit(element: JsonElement): Boolean {
        if (element.isJsonObject) {
            val asObject = element.obj
            if (asObject.has("retry_after")) {
                val retryAfter = asObject["retry_after"].long

                logger.info { "Got rate limited, oof! Retry After: $retryAfter" }
                // oof, ratelimited!
                delay(retryAfter)
                throw RateLimitedException()
            }
        }

        return false
    }

    private suspend fun checkIfRequestWasValid(response: HttpResponse): HttpResponse {
        if (response.status.value == 401)
            throw TokenUnauthorizedException(response.status)

        return response
    }

    suspend fun getSelfUserInfo(): TwitchUser {
        val response = makeTwitchApiRequest("$API_PREFIX/helix/users") {}

        return JsonIgnoreUnknownKeys.decodeFromString<GetUsersResponse>(response.bodyAsText())
            .data
            .first()
    }

    suspend fun getUsersInfoByLogin(vararg logins: String): List<TwitchUser> {
        if (logins.isEmpty())
            return emptyList()

        val response = makeTwitchApiRequest("$API_PREFIX/helix/users") {
            for (login in logins) {
                parameter("login", login)
            }
        }

        return JsonIgnoreUnknownKeys.decodeFromString<GetUsersResponse>(response.bodyAsText())
            .data
    }

    suspend fun getUsersInfoById(vararg ids: Long): List<TwitchUser> {
        if (ids.isEmpty())
            return emptyList()

        val response = makeTwitchApiRequest("$API_PREFIX/helix/users") {
            for (id in ids) {
                parameter("id", id)
            }
        }

        return JsonIgnoreUnknownKeys.decodeFromString<GetUsersResponse>(response.bodyAsText())
            .data
    }

    suspend fun getStreamsByUserId(vararg ids: Long): List<TwitchStream> {
        if (ids.isEmpty())
            return emptyList()

        val response = makeTwitchApiRequest("$API_PREFIX/helix/streams") {
            for (id in ids) {
                parameter("user_id", id)
            }
        }

        return JsonIgnoreUnknownKeys.decodeFromString<GetStreamsResponse>(response.bodyAsText())
            .data
    }

    suspend fun createSubscription(subscriptionRequest: SubscriptionCreateRequest): SubscriptionCreateResponse {
        val response = makeTwitchApiRequest("$API_PREFIX/helix/eventsub/subscriptions") {
            method = HttpMethod.Post

            setBody(TextContent(Json.encodeToString(subscriptionRequest), ContentType.Application.Json))
        }

        if (!response.status.isSuccess()) {
            val json = Json.parseToJsonElement(response.bodyAsText())
                .jsonObject
            val message = json["message"]?.jsonPrimitive?.content

            if (message == "cannot create a subscription for a user that does not exist") {
                throw SubscriptionCreateForUnknownUserException(json)
            } else {
                throw SubscriptionCreateException(json)
            }
        }

        return JsonIgnoreUnknownKeys.decodeFromString<SubscriptionCreateResponse>(response.bodyAsText())
    }

    suspend fun deleteSubscription(subscriptionId: String) {
        val response = makeTwitchApiRequest("$API_PREFIX/helix/eventsub/subscriptions?id=$subscriptionId") {
            method = HttpMethod.Delete
        }
    }

    suspend fun loadAllSubscriptions(): List<SubscriptionListResponse> {
        val subscriptions = mutableListOf<SubscriptionListResponse>()

        var cursor: String? = null
        var first = true
        while (first || cursor != null) {
            val subscriptionListData = loadSubscriptions(cursor)
            cursor = subscriptionListData.pagination.cursor
            first = false
            subscriptions.add(subscriptionListData)
        }

        return subscriptions
    }

    suspend fun loadSubscriptions(cursor: String? = null): SubscriptionListResponse {
        return makeTwitchApiRequest("$API_PREFIX/helix/eventsub/subscriptions") {
            method = HttpMethod.Get
            if (cursor != null)
                parameter("after", cursor)
        }
            .bodyAsText()
            .let { Json.decodeFromString(it) }
    }

    suspend fun makeTwitchApiRequest(url: String, httpRequestBuilderBlock: HttpRequestBuilder.() -> (Unit)): HttpResponse {
        return doStuff {
            logger.info { "Executing Twitch request $url..." }
            val result = checkIfRequestWasValid(
                http.request(url) {
                    userAgent(USER_AGENT)
                    header("Authorization", "Bearer $accessToken")
                    header("Client-ID", clientId)

                    httpRequestBuilderBlock.invoke(this)
                }
            )
            logger.info { "Twitch request $url executed successfully! Status: ${result.status}" }
            result
        }
    }

    class TokenUnauthorizedException(status: HttpStatusCode) : RuntimeException()
    class TokenExchangeException(message: String) : RuntimeException(message)
    open class SubscriptionCreateException(json: kotlinx.serialization.json.JsonObject) : RuntimeException(json.toString())
    class SubscriptionCreateForUnknownUserException(json: kotlinx.serialization.json.JsonObject) : SubscriptionCreateException(json)
    private class RateLimitedException : RuntimeException()
    private class NeedsRefreshException : RuntimeException()
}