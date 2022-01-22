package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.Snowflake
import dev.kord.rest.json.request.WebhookEditMessageRequest
import dev.kord.rest.json.request.WebhookExecuteRequest
import dev.kord.rest.ratelimit.ExclusionRequestRateLimiter
import dev.kord.rest.ratelimit.RequestRateLimiter
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.request.RequestBuilder
import dev.kord.rest.request.RequestHandler
import dev.kord.rest.route.Route
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Utilities related to webhooks
 */
object WebhookUtils {
    val webhookRegex = Regex("/api/webhooks/([0-9]+)/([A-z0-9_-]+)")

    private val jsonDefault = Json {
        encodeDefaults = false
        allowStructuredMapKeys = true
        ignoreUnknownKeys = true
        isLenient = true
    }

    val requestHandlerWithoutToken = KtorRequestHandlerWithoutToken()

    /**
     * Creates a Ktor [RequestHandler] that does not require a bot token.
     *
     * Useful because if you are using a bot token, Discord will rate limit based on the bot token too, causing global rate limits.
     */
    fun KtorRequestHandlerWithoutToken(
        requestRateLimiter: RequestRateLimiter = ExclusionRequestRateLimiter(),
        clock: Clock = Clock.System,
        parser: Json = jsonDefault,
    ): KtorRequestHandler {
        val client = HttpClient(CIO) {
            expectSuccess = false
        }
        return KtorRequestHandler(client, requestRateLimiter, clock, parser, "")
    }

    // Same thing as Kord's "executeWebhook" method, but we accept a WebhookExecuteRequest class instead of a builder
    // TODO: Support files
    @OptIn(ExperimentalContracts::class)
    suspend inline fun executeWebhook(
        webhookId: Snowflake,
        token: String,
        wait: Boolean? = null,
        threadId: Snowflake? = null,
        request: WebhookExecuteRequest
    ): DiscordMessage? {
        return call(Route.ExecuteWebhookPost, requestHandlerWithoutToken) {
            keys[Route.WebhookId] = webhookId
            keys[Route.WebhookToken] = token
            if(wait != null) parameter("wait", "$wait")
            if(threadId != null) parameter("thread_id", threadId.asString)
            body(WebhookExecuteRequest.serializer(), request)
        }
    }

    // Same thing as Kord's "editWebhookMessage" method, but we accept a WebhookExecuteRequest class instead of a builder
    // TODO: Support files
    @OptIn(ExperimentalContracts::class)
    suspend inline fun editWebhookMessage(
        webhookId: Snowflake,
        token: String,
        messageId: Snowflake,
        request: WebhookEditMessageRequest
    ): DiscordMessage {
        return call(Route.EditWebhookMessage, requestHandlerWithoutToken) {
            keys[Route.WebhookId] = webhookId
            keys[Route.WebhookToken] = token
            keys[Route.MessageId] = messageId
            body(WebhookEditMessageRequest.serializer(), request)
        }
    }

    @OptIn(ExperimentalContracts::class)
    @PublishedApi
    internal suspend inline fun <T> call(route: Route<T>, requestHandler: RequestHandler, builder: RequestBuilder<T>.() -> Unit = {}): T {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }
        val request = RequestBuilder(route).apply(builder).build()
        return requestHandler.handle(request)
    }
}