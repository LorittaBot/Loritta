package net.perfectdreams.loritta.dashboard.backend

import io.ktor.client.*
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.dashboard.backend.configs.LorittaDashboardBackendConfig
import net.perfectdreams.loritta.dashboard.backend.utils.writeSseEvent

class LorittaDashboardBackend(val config: LorittaDashboardBackendConfig) {
    companion object {
        val ALLOWED_REQUEST_HEADERS = setOf(
            "User-Agent",
            "Cookie",
            "Bliss-Request",
            "Bliss-Trigger-Element-Id",
            "Bliss-Trigger-Element-Name",
            "Accept-Language",
            "Referer"
        ).map { it.lowercase() }.toSet()

        val ALLOWED_RESPONSE_HEADERS = setOf(
            "Location",
            "Set-Cookie",
            "Bliss-Redirect",
            "Bliss-Push-Url",
            "Bliss-Refresh",
            "X-Accel-Buffering",
            "Loritta-Cluster",

            // Caching
            "Age",
            "Cache-Control",
            "Expires"
        ).map { it.lowercase() }.toSet()

        val PROXIED_METHODS = setOf(
            HttpMethod.Get,
            HttpMethod.Post,
            HttpMethod.Put,
            HttpMethod.Patch,
            HttpMethod.Delete
        )

        val logger by HarmonyLoggerFactory.logger {}
    }

    val http = HttpClient(Java) {
        this.expectSuccess = false
        this.followRedirects = false

        install(SSE)
    }

    fun start() {
        val server = embeddedServer(Netty, port = 8080) {
            routing {
                get("/hewwo") {
                    call.respondText("""Loritta's Dashboard Proxy - Loritta is so cute!! :3""")
                }

                route("/{localeId}/guilds/{guildId}/{tail...}") {
                    for (method in PROXIED_METHODS) {
                        method(method) {
                            handle {
                                val full = call.request.path()
                                var queryString = call.request.queryString()
                                if (queryString.isNotEmpty())
                                    queryString = "?$queryString"

                                val guildHost = getLorittaClusterForGuildId(call.parameters["guildId"]!!.toLong()).dashboardBaseAPIUrl

                                doProxy(call, method, guildHost, "$full$queryString")
                            }
                        }
                    }
                }

                route("/{...}") {
                    for (method in PROXIED_METHODS) {
                        method(method) {
                            handle {
                                val full = call.request.path()
                                var queryString = call.request.queryString()
                                if (queryString.isNotEmpty())
                                    queryString = "?$queryString"

                                doProxy(call, method, config.clusters.first { it.id == 1 }.dashboardBaseAPIUrl, "$full$queryString")
                            }
                        }
                    }
                }
            }
        }

        server.start(wait = true)
    }

    suspend fun doProxy(call: ApplicationCall, method: HttpMethod, host: String, path: String) {
        val pathWithoutSlashPrefix = path.removePrefix("/")
        val acceptHeader = call.request.accept()?.let {
            ContentType.parse(it)
        }

        if (acceptHeader?.match(ContentType.Text.EventStream) == true) {
            logger.info { "Requesting $method $host$pathWithoutSlashPrefix (SSE)..." }
            // SSE is a bit trickier!
            // But because SSE are my beloved, we *need* to support them :3
            http.sse(
                "$host$pathWithoutSlashPrefix",
                {
                    header("Dashboard-Proxy", "true")
                    header("X-Forwarded-Host", call.request.header("X-Forwarded-Host") ?: call.request.header("Host"))
                    header("X-Forwarded-Proto", call.request.header("X-Forwarded-Proto") ?: "http")

                    for (header in call.request.headers.entries()) {
                        val headerLowercase = header.key.lowercase() // The headers themselves are case-insensitive

                        if (headerLowercase in ALLOWED_REQUEST_HEADERS) {
                            for (value in header.value) {
                                var _value = value

                                for (entry in config.cookieReplacers) {
                                    _value = _value.replace(entry.to, entry.from)
                                }

                                header(
                                    header.key,
                                    _value
                                )
                            }
                        }
                    }
                }
            ) {
                for (header in this.call.response.headers.entries()) {
                    if (header.key.lowercase() in ALLOWED_RESPONSE_HEADERS) {
                        for (value in header.value) {
                            var _value = value

                            for (entry in config.cookieReplacers) {
                                _value = _value.replace(entry.from, entry.to)
                            }

                            call.response.header(
                                header.key,
                                _value
                            )
                        }
                    }
                }

                call.respondBytesWriter(contentType = ContentType.Text.EventStream) {
                    this@sse.incoming.collect {
                        writeSseEvent(it)
                        flush()
                    }
                }
            }
        } else {
            logger.info { "Requesting $method $host$pathWithoutSlashPrefix..." }

            val requestContentLength = call.request.contentLength()
            val httpResponse = http.request("$host$pathWithoutSlashPrefix") {
                this.method = method

                if (acceptHeader != null)
                    accept(acceptHeader)

                header("Dashboard-Proxy", "true")
                header("X-Forwarded-Host", call.request.header("X-Forwarded-Host") ?: call.request.header("Host"))
                header("X-Forwarded-Proto", call.request.header("X-Forwarded-Proto") ?: "http")

                for (header in call.request.headers.entries()) {
                    val headerLowercase = header.key.lowercase() // The headers themselves are case-insensitive

                    if (headerLowercase in ALLOWED_REQUEST_HEADERS) {
                        for (value in header.value) {
                            var _value = value

                            for (entry in config.cookieReplacers) {
                                _value = _value.replace(entry.to, entry.from)
                            }

                            header(
                                header.key,
                                _value
                            )
                        }
                    }
                }

                // There's a bug in some bad clients (Bot Designer for Discord) that they send a "Content-Type" header for GET requests without any body, even if that's incorrect
                // So, as an workaround, we'll only attempt to read the body only if the request is NOT a GET request
                // The reason we do this is that somewhere (not in Ktor) there's a ~15s timeout waiting for the client to send a body, and that's causing issues
                // ...but then I found out that this same behavior *also* happens with curl, if you do a POST without any body
                // so as a 100% workaround, we'll check if the Content-Length is not null and if it is larger than 0
                // This is also useful when working against the browser!
                if (requestContentLength != null && requestContentLength > 0) {
                    setBody(
                        ByteArrayContent(
                            call.receiveStream().readAllBytes(),
                            call.request.contentType()
                        )
                    )
                }
            }
            logger.info { "Request $method $host$pathWithoutSlashPrefix status is ${httpResponse.status}" }

            for (header in httpResponse.headers.entries()) {
                if (header.key.lowercase() in ALLOWED_RESPONSE_HEADERS) {
                    for (value in header.value) {
                        var _value = value

                        for (entry in config.cookieReplacers) {
                            _value = _value.replace(entry.from, entry.to)
                        }

                        call.response.header(
                            header.key,
                            _value
                        )
                    }
                }
            }

            val originalResponse = httpResponse.bodyAsBytes()
            val responseContentType = httpResponse.contentType() ?: ContentType.Any
            if (responseContentType.match(ContentType.Text.Html)) {
                var htmlResponse = originalResponse.toString(Charsets.UTF_8)

                for (entry in config.replacers) {
                    htmlResponse = htmlResponse.replace(entry.from, entry.to)
                }

                call.respondBytes(
                    htmlResponse.toByteArray(Charsets.UTF_8),
                    status = httpResponse.status,
                    contentType = responseContentType
                )
            } else {
                call.respondBytes(
                    originalResponse,
                    status = httpResponse.status,
                    contentType = responseContentType
                )
            }
        }
    }

    /**
     * Gets a Discord Shard ID from the provided Guild ID
     *
     * @return the shard ID
     */
    fun getLorittaClusterForGuildId(id: Long): LorittaDashboardBackendConfig.LorittaClusterConfig {
        val shardId = getShardIdFromGuildId(id)
        return getLorittaClusterForShardId(shardId)
    }

    /**
     * Gets a Discord Shard ID from the provided Guild ID
     *
     * @return the shard ID
     */
    fun getShardIdFromGuildId(id: Long) = getShardIdFromGuildId(id, config.totalShards)

    /**
     * Gets a Discord Shard ID from the provided Guild ID
     *
     * @return the shard ID
     */
    fun getShardIdFromGuildId(id: Long, maxShards: Int) = (id shr 22).rem(maxShards).toInt()

    /**
     * Gets the cluster where the guild that has the specified ID is in
     *
     * @return the cluster
     */
    fun getLorittaClusterForShardId(id: Int): LorittaDashboardBackendConfig.LorittaClusterConfig {
        val lorittaShard = config.clusters.firstOrNull { id in it.minShard..it.maxShard }
        return lorittaShard ?: throw RuntimeException("Frick! I don't know what is the Loritta Shard for Discord Shard ID $id")
    }
}