package net.perfectdreams.loritta.dashboard.backend

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
import net.perfectdreams.loritta.dashboard.backend.configs.LorittaDashboardBackendConfig

class LorittaDashboardBackend(val config: LorittaDashboardBackendConfig) {
    companion object {
        val ALLOWED_REQUEST_HEADERS = setOf(
            "Cookie",
            "HX-Request",
            "HX-Trigger"
        ).map { it.lowercase() }.toSet()

        val ALLOWED_RESPONSE_HEADERS = setOf(
            "Location",
            "Set-Cookie",
            "HX-Vary",
            "HX-Redirect",
            "HX-Trigger",
            "SpicyMorenitta-Use-Response-As-HXTrigger",
            "SpicyMorenitta-Open-Embedded-Spicy-Modal"
        ).map { it.lowercase() }.toSet()

        val PROXIED_METHODS = setOf(
            HttpMethod.Get,
            HttpMethod.Post,
            HttpMethod.Put,
            HttpMethod.Patch,
            HttpMethod.Delete
        )
    }

    val http = HttpClient(io.ktor.client.engine.cio.CIO) {
        this.expectSuccess = false
        this.followRedirects = false
    }

    fun start() {
        val server = embeddedServer(CIO, port = 8080) {
            routing {
                get("/hewwo") {
                    call.respondText("""Loritta's Dashboard Proxy - Loritta is so cute!! :3""")
                }

                route("/{localeId}/guild/{guildId}/{tail...}") {
                    for (method in PROXIED_METHODS) {
                        method(method) {
                            handle {
                                val full = call.request.path()
                                var queryString = call.request.queryString()
                                if (queryString.isNotEmpty())
                                    queryString = "?$queryString"

                                val guildHost = getLorittaClusterForGuildId(call.parameters["guildId"]!!.toLong()).websiteInternalUrl

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

                                doProxy(call, method, config.clusters.first { it.id == 1 }.websiteInternalUrl, "$full$queryString")
                            }
                        }
                    }
                }
            }
        }

        server.start(wait = true)
    }

    suspend fun doProxy(call: ApplicationCall, method: HttpMethod, host: String, path: String) {
        val httpResponse = http.request("$host$path") {
            this.method = method

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

            setBody(
                ByteArrayContent(
                    call.receiveStream().readAllBytes(),
                    call.request.contentType()
                )
            )
        }

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