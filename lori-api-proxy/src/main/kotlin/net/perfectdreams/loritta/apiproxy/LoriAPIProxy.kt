package net.perfectdreams.loritta.apiproxy

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*
import mu.KotlinLogging
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

/**
 * Loritta's API Proxy
 *
 * The API Proxy is meant for public usage, the API proxy, just like internal applications, use Loritta's internal API directly
 */
class LoriAPIProxy(
    val http: HttpClient,
    val lorittaInfo: LorittaInternalRPCResponse.GetLorittaInfoResponse.Success,
) {
    companion object {
        private val PROXIED_HEADERS_TO_BACKEND = setOf(
            "User-Agent",
            "Authorization"
        )
        private val logger = KotlinLogging.logger {}
    }

    private val proxiedRoutes = listOf(
        ProxiedRoute(HttpMethod.Get, "/v1/users/{userId}", ProxiedRoute.ROUTE_TO_DEFAULT_CLUSTER),
        ProxiedRoute(HttpMethod.Get, "/v1/users/{userId}/transactions", ProxiedRoute.ROUTE_TO_DEFAULT_CLUSTER),
        ProxiedRoute(HttpMethod.Get, "/v1/sonhos/rank", ProxiedRoute.ROUTE_TO_DEFAULT_CLUSTER),
        ProxiedRoute(HttpMethod.Post, "/v1/lori-messages/verify-message", ProxiedRoute.ROUTE_TO_DEFAULT_CLUSTER),
        ProxiedRoute(HttpMethod.Post, "/v1/guilds/{guildId}/channels/{channelId}/messages/{messageId}/save", ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(HttpMethod.Put, "/v1/guilds/{guildId}/giveaways", ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(HttpMethod.Post, "/v1/guilds/{guildId}/giveaways/{giveawayId}/end", ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(HttpMethod.Post, "/v1/guilds/{guildId}/giveaways/{giveawayId}/reroll", ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(HttpMethod.Get, "/v1/guilds/{guildId}/emojifights/top-winners", ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(HttpMethod.Get, "/v1/guilds/{guildId}/users/{userId}/emojifight/victories", ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
    )

    fun start() {
        val server = embeddedServer(CIO) {
            routing {
                get("/") {
                    call.respondText(
                        buildString {
                            appendLine("Loritta's API Proxy")
                            appendLine()
                            appendLine("https://youtu.be/Ka4MZzdScRI")
                            appendLine()
                            val howMuchToPad = proxiedRoutes.maxOf {
                                it.method.value.length
                            }
                            for (proxiedRoute in proxiedRoutes) {
                                appendLine("${proxiedRoute.method.value.padEnd(howMuchToPad, ' ')} ${proxiedRoute.path}")
                            }
                        }
                    )
                }

                for (proxiedRoute in proxiedRoutes) {
                    route(proxiedRoute.path, proxiedRoute.method) {
                        handle {
                            val authorizationTokenFromHeader = call.request.header("Authorization")
                            val clusterToBeUsed = proxiedRoute.routeToClusterId.invoke(this@LoriAPIProxy, call)

                            // The URI already includeds the query string
                            logger.info { "Requesting ${proxiedRoute.method} ${call.request.uri} for $authorizationTokenFromHeader..." }
                            val response = http.request("${clusterToBeUsed.rpcUrl.removeSuffix("/")}/lori-public-api${call.request.uri}") {
                                this.method = proxiedRoute.method

                                for (header in PROXIED_HEADERS_TO_BACKEND) {
                                    val clientHeader = call.request.header(header)
                                    if (clientHeader != null)
                                        header(header, clientHeader)
                                }

                                setBody(call.receiveStream())
                            }
                            logger.info { "Requested ${proxiedRoute.method.value} ${call.request.uri} for $authorizationTokenFromHeader! Status Code: ${response.status}" }

                            val proxiedHeaders = response.headers
                            val location = proxiedHeaders[HttpHeaders.Location]
                            val contentType = proxiedHeaders[HttpHeaders.ContentType]
                            val contentLength = proxiedHeaders[HttpHeaders.ContentLength]

                            call.respond(object : OutgoingContent.WriteChannelContent() {
                                override val contentLength: Long? = contentLength?.toLong()
                                override val contentType: ContentType? = contentType?.let { ContentType.parse(it) }
                                override val headers: Headers = Headers.build {
                                    appendAll(proxiedHeaders.filter { key, _ ->
                                        !key.equals(
                                            HttpHeaders.ContentType,
                                            ignoreCase = true
                                        ) && !key.equals(HttpHeaders.ContentLength, ignoreCase = true)
                                    })
                                }
                                override val status: HttpStatusCode = response.status
                                override suspend fun writeTo(channel: ByteWriteChannel) {
                                    response.bodyAsChannel().copyAndClose(channel)
                                }
                            })
                        }
                    }
                }
            }
        }

        server.start(true)
    }
}