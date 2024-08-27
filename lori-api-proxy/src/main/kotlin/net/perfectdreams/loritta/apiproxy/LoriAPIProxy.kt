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
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
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
        private val BACKEND_HEADERS_TO_BE_LOGGED = setOf(
            "Loritta-Cluster",
            "Loritta-Token-Creator",
            "Loritta-Token-User"
        )
        private val logger = KotlinLogging.logger {}
    }

    private val proxiedRoutes = listOf(
        ProxiedRoute(LoriPublicHttpApiEndpoints.GET_USER_BY_ID, ProxiedRoute.ROUTE_TO_DEFAULT_CLUSTER),
        ProxiedRoute(LoriPublicHttpApiEndpoints.GET_USER_TRANSACTIONS, ProxiedRoute.ROUTE_TO_DEFAULT_CLUSTER),
        ProxiedRoute(LoriPublicHttpApiEndpoints.GET_SONHOS_RANK, ProxiedRoute.ROUTE_TO_DEFAULT_CLUSTER),
        ProxiedRoute(LoriPublicHttpApiEndpoints.VERIFY_LORITTA_MESSAGE, ProxiedRoute.ROUTE_TO_DEFAULT_CLUSTER),
        ProxiedRoute(LoriPublicHttpApiEndpoints.SAVE_LORITTA_MESSAGE, ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(LoriPublicHttpApiEndpoints.CREATE_GUILD_GIVEAWAY, ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(LoriPublicHttpApiEndpoints.END_GUILD_GIVEAWAY, ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(LoriPublicHttpApiEndpoints.REROLL_GUILD_GIVEAWAY, ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(LoriPublicHttpApiEndpoints.EMOJIFIGHT_GUILD_TOP_WINNERS_RANK, ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
        ProxiedRoute(LoriPublicHttpApiEndpoints.EMOJIFIGHT_GUILD_VICTORIES, ProxiedRoute.ROUTE_BASED_ON_GUILD_ID),
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
                            val clientHeaders = call.request.headers.toMap().entries.joinToString("; ") {
                                "$it: ${it.value}"
                            }
                            logger.info { "Requesting ${proxiedRoute.method.value} ${call.request.uri} for $authorizationTokenFromHeader... $clientHeaders" }
                            val requestContentLength = call.request.contentLength()
                            val response = http.request("${clusterToBeUsed.rpcUrl.removeSuffix("/")}/lori-public-api${call.request.uri}") {
                                this.method = proxiedRoute.method

                                for (header in PROXIED_HEADERS_TO_BACKEND) {
                                    val clientHeader = call.request.header(header)
                                    if (clientHeader != null)
                                        header(header, clientHeader)
                                }

                                // There's a bug in some bad clients (Bot Designer for Discord) that they send a "Content-Type" header for GET requests without any body, even if that's incorrect
                                // So, as an workaround, we'll only attempt to read the body only if the request is NOT a GET request
                                // The reason we do this is that somewhere (not in Ktor) there's a ~15s timeout waiting for the client to send a body, and that's causing issues
                                // ...but then I found out that this same behavior *also* happens with curl, if you do a POST without any body
                                // so as a 100% workaround, we'll check if the Content-Length is not null and if it is larger than 0
                                if (requestContentLength != null && requestContentLength > 0) {
                                    setBody(call.receiveStream())
                                }
                            }
                            val logBuild = BACKEND_HEADERS_TO_BE_LOGGED.joinToString("; ") {
                                "$it: ${response.headers[it]}"
                            }

                            logger.info { "Requested ${proxiedRoute.method.value} ${call.request.uri} for $authorizationTokenFromHeader! Status Code: ${response.status}; $logBuild" }

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