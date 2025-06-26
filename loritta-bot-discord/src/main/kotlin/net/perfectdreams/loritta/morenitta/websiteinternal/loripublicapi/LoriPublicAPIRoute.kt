package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserLorittaAPITokens
import net.perfectdreams.loritta.common.utils.TokenType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoint
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.selectAll

abstract class LoriPublicAPIRoute(
    val m: LorittaBot,
    val endpoint: LoriPublicHttpApiEndpoint,
    val rateLimitOptions: RateLimitOptions
) : BaseRoute("/lori-public-api/v1${endpoint.path}") {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    private val accesses = mutableMapOf<String, RateLimitData>()
    private val rateLimitCheckMutex = Mutex()

    override suspend fun onRequest(call: ApplicationCall) {
        call.response.header("Loritta-Cluster", m.lorittaCluster.getUserAgent(m))

        val authorizationToken = call.request.header("Authorization")
        val clazzName = this::class.simpleName
        if (authorizationToken == null) {
            logger.warn { "Someone tried to access $path (${clazzName}) but the Authorization header was missing!" }
            // Not authorized/missing Authorization header
            call.respondJson(
                "",
                status = HttpStatusCode.Unauthorized
            )
            return
        }

        logger.info { "$authorizationToken is trying to access $path (${clazzName})" }

        // TODO: Maybe a token rate limit would be cool too?
        val tokenInfo = m.transaction {
            UserLorittaAPITokens.selectAll()
                .where {
                    UserLorittaAPITokens.token eq authorizationToken
                }
                .limit(1)
                .firstOrNull()
        }

        if (tokenInfo == null) {
            logger.warn { "$authorizationToken was rejected when trying to access $path ($clazzName) because the token is invalid!" }
            // Invalid token
            call.respondJson(
                "",
                status = HttpStatusCode.Unauthorized
            )
            return
        }

        val tokenInfoData = TokenInfo(
            tokenInfo[UserLorittaAPITokens.token],
            tokenInfo[UserLorittaAPITokens.tokenType] ?: TokenType.USER,
            tokenInfo[UserLorittaAPITokens.tokenCreatorId],
            tokenInfo[UserLorittaAPITokens.tokenUserId]
        )
        logger.info { "${tokenInfoData.token} is accessing $path ($clazzName) on behalf of ${tokenInfoData.userId} (created by ${tokenInfoData.creatorId})" }

        // This is who created the token
        call.response.header("Loritta-Token-Creator", tokenInfoData.creatorId)
        // This is who we are "acting" as (because we may be acting on behalf of a bot!)
        call.response.header("Loritta-Token-User", tokenInfoData.userId)

        // Ratelimit checks should be inside a lock to avoid concurrency issues
        val rateLimitResult = rateLimitCheckMutex.withLock {
            // Clean up any expired ratelimits
            val tokensToBeRemoved = mutableSetOf<String>()
            accesses.forEach { t, u ->
                if (kotlinx.datetime.Clock.System.now() > u.expiresAfter) {
                    tokensToBeRemoved.add(t)
                }
            }
            for (tokenToBeRemoved in tokensToBeRemoved) {
                accesses.remove(tokenToBeRemoved)
            }

            val rateLimitData = accesses.getOrPut(authorizationToken) {
                RateLimitData(0, Clock.System.now() + rateLimitOptions.rateLimitResetAfter)
            }

            if (rateLimitData.requestsMade == rateLimitOptions.totalAllowedRequests)
                return@withLock RateLimitResult.Blocked(rateLimitData)

            rateLimitData.requestsMade++
            return@withLock RateLimitResult.Success(rateLimitData)
        }

        // Example Discord Ratelimit Headers
        // X-RateLimit-Limit: 5
        // X-RateLimit-Remaining: 0
        // X-RateLimit-Reset: 1470173023
        // X-RateLimit-Reset-After: 1
        call.response.header("X-RateLimit-Limit", rateLimitOptions.totalAllowedRequests)
        call.response.header("X-RateLimit-Remaining", rateLimitOptions.totalAllowedRequests - rateLimitResult.data.requestsMade)
        call.response.header("X-RateLimit-Reset", rateLimitResult.data.expiresAfter.toEpochMilliseconds() / 1_000)
        // I think there's a way for the resetAfter value to be negative due to delays, but it shouldn't *really* matter
        val resetAfter = (rateLimitResult.data.expiresAfter - Clock.System.now()).inWholeMilliseconds / 1_000.0
        call.response.header("X-RateLimit-Reset-After", resetAfter.toString())

        if (rateLimitResult is RateLimitResult.Blocked) {
            call.response.header("Retry-After", resetAfter.toString())
            call.respondJson(
                Json.encodeToString(
                    RateLimitedResponse(
                        resetAfter
                    )
                ),
                status = HttpStatusCode.TooManyRequests
            )
            return
        }

        onAPIRequest(
            call,
            tokenInfoData
        )
    }

    abstract suspend fun onAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo)

    override fun getMethod() = endpoint.method

    data class RateLimitData(
        var requestsMade: Int,
        var expiresAfter: kotlinx.datetime.Instant
    )

    sealed class RateLimitResult(val data: RateLimitData) {
        class Blocked(data: RateLimitData) : RateLimitResult(data)
        class Success(data: RateLimitData) : RateLimitResult(data)
    }

    @Serializable
    data class RateLimitedResponse(
        val retryAfter: Double
    )
}