package net.perfectdreams.loritta.deviousfun.requests

import dev.kord.rest.ratelimit.*
import dev.kord.rest.request.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import kotlin.time.Duration.Companion.seconds

internal val jsonDefault = Json {
    encodeDefaults = false
    allowStructuredMapKeys = true
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * A [RequestHandler] that uses ktor's [HttpClient][client] to execute requests and accepts a [requestRateLimiter]
 * to schedule requests.
 *
 * @param client A [HttpClient] configured with the required headers for identification.
 * @param clock A [Clock] to calculate bucket reset times, exposed for testing.
 * @param parser Serializer used to parse payloads.
 */
public class DeviousKtorRequestHandler(
    private val client: HttpClient,
    private val clock: Clock = Clock.System,
    private val parser: Json = jsonDefault,
    override val token: String,
) : RequestHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    private val rateLimiter = DeviousRateLimiter(clock)

    override suspend fun <B : Any, R> handle(request: Request<B, R>): R {
        val recoveredStackTrace = RecoveredStackTrace()

        try {
            // Before executing let's check if we need to await for the rate limit
            val response = rateLimiter.addRequestToQueue(request) {
                // Create the request
                val httpRequest = client.createRequest(request)
                // Execute the request
                val response = httpRequest.execute()

                rateLimiter.updateRateLimitStatus(request, response)

                if (response.status == HttpStatusCode.TooManyRequests) {
                    // Oh no, we are rate limited!
                    val isGlobal = response.headers["X-RateLimit-Global"] != null
                    val isCloudflare = response.headers["via"] == null
                    if (isGlobal) {
                        val retryAfterHeader = response.headers[DeviousRateLimiter.RETRY_AFTER_HEADER] ?: error("Missing ${DeviousRateLimiter.RETRY_AFTER_HEADER} globally rate limited request!")
                        val retryAfter = retryAfterHeader.toLong()
                        return@addRequestToQueue DeviousRateLimiter.RateLimitResponse.GloballyRateLimited(retryAfter.seconds)
                    }
                    if (isCloudflare) {
                        val retryAfterHeader = response.headers[DeviousRateLimiter.RETRY_AFTER_HEADER] ?: error("Missing ${DeviousRateLimiter.RETRY_AFTER_HEADER} cloudflare rate limited request!")
                        val retryAfter = retryAfterHeader.toLong()
                        return@addRequestToQueue DeviousRateLimiter.RateLimitResponse.CloudFlareRateLimited(retryAfter.seconds)
                    }

                    logger.warn { "Received 429 on bucket ${it.bucketId} (${request.route})!" }

                    // Because this request got rate limited, we will retry...
                    return@addRequestToQueue DeviousRateLimiter.RateLimitResponse.RateLimited
                }

                return@addRequestToQueue DeviousRateLimiter.RateLimitResponse.Success(response)
            }

            // Let's figure out what happened!
            val body = response.bodyAsText()

            return when {
                response.isRateLimit -> {
                    logger.debug { response.logString(body) }
                    handle(request)
                }

                response.isError -> {
                    logger.debug { response.logString(body) }
                    if (response.contentType() == ContentType.Application.Json)
                        throw KtorRequestException(
                            response, request, if (body.isNotBlank()) Json.decodeFromString(body) else null
                        )
                    else throw KtorRequestException(response, request, null)
                }

                else -> {
                    logger.debug { response.logString(body) }
                    request.route.mapper.deserialize(parser, body)
                }
            }
        } catch (e: Throwable) {
            recoveredStackTrace.sanitizeStackTrace()
            e.addSuppressed(recoveredStackTrace)
            throw e
        }
    }

    private suspend fun <B : Any, R> HttpClient.createRequest(request: Request<B, R>) = prepareRequest {
        method = request.route.method
        headers.appendAll(request.headers)

        url {
            url.takeFrom(request.baseUrl)
            encodedPath += request.path
            parameters.appendAll(request.parameters)
        }

        when (request) {
            is JsonRequest -> run {
                val requestBody = request.body ?: return@run
                val json = parser.encodeToString(requestBody.strategy, requestBody.body)
                logger.debug { request.logString(json) }
                setBody(TextContent(json, ContentType.Application.Json))
            }
            is MultipartRequest -> {
                val content = request.data
                setBody(MultiPartFormDataContent(content))
                logger.debug {
                    val json = content.filterIsInstance<PartData.FormItem>()
                        .firstOrNull { it.name == "payload_json" }?.value
                    request.logString(json ?: "")
                }
            }
        }

    }
}

public fun DeviousKtorRequestHandler(
    token: String,
    clock: Clock = Clock.System,
    parser: Json = jsonDefault,
): DeviousKtorRequestHandler {
    val client = HttpClient(CIO) {
        expectSuccess = false
    }
    return DeviousKtorRequestHandler(client, clock, parser, token)
}

/** A [Throwable] used to save the current stack trace before executing a request. */
internal class RecoveredStackTrace : Throwable("This is the recovered stack trace:") {

    fun sanitizeStackTrace() {
        // Remove artifacts of stack trace capturing.
        // The first stack trace element is the creation of the RecoveredStackTrace:
        // at dev.kord.rest.request.StackTraceRecoveringKtorRequestHandler.handle(StackTraceRecoveringKtorRequestHandler.kt:21)
        stackTrace = stackTrace.copyOfRange(1, stackTrace.size)
    }
}