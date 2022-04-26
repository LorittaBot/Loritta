package net.perfectdreams.randomroleplaypictures.backend

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import net.perfectdreams.randomroleplaypictures.backend.plugins.configureRouting
import net.perfectdreams.randomroleplaypictures.backend.routes.HugPicturesRoute

class RandomRoleplayPictures {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val typesToCache = listOf(
        ContentType.Text.CSS,
        ContentType.Text.JavaScript,
        ContentType.Application.JavaScript,
        ContentType.Image.Any,
        ContentType.Video.Any
    )

    val routes = listOf(
        HugPicturesRoute()
    )

    fun start() {
        val server = embeddedServer(Netty, port = 8080) {
            // Enables gzip and deflate compression
            install(Compression)

            // Enables caching for the specified types in the typesToCache list
            install(CachingHeaders) {
                options { call, outgoingContent ->
                    val contentType = outgoingContent.contentType
                    if (contentType != null) {
                        val contentTypeWithoutParameters = contentType.withoutParameters()
                        val matches = typesToCache.any { contentTypeWithoutParameters.match(it) || contentTypeWithoutParameters == it }

                        if (matches)
                            CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 365 * 24 * 3600))
                        else
                            null
                    } else null
                }
            }

            configureRouting(this@RandomRoleplayPictures, routes)

            routing {
                static("/assets/") {
                    resources("static/")
                }
            }
        }
        server.start(true)
    }
}