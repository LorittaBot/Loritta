package net.perfectdreams.loritta.morenitta.website.routes.httpapidocs

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.LocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoint
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import java.util.*
import kotlin.reflect.full.declaredMembers

class PostTestLoriDevelopersDocsEndpointRoute(loritta: LorittaBot) : LocalizedRoute(loritta, "/developers/docs/endpoint-tester") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val http = HttpClient {}
    val headersToBeShown = setOf(
        "Loritta-Cluster",
        "Loritta-Token-Creator",
        "Loritta-Token-User",
        "Content-Type",
        "X-RateLimit-Limit",
        "X-RateLimit-Remaining",
        "X-RateLimit-Reset",
        "X-RateLimit-Reset-After",
        "Retry-After",
    )

    override val isMainClusterOnlyRoute = true

    override suspend fun onLocalizedRequest(
        call: ApplicationCall,
        locale: BaseLocale,
        i18nContext: I18nContext
    ) {
        val session = call.lorittaSession
        val discordAuth = session.getDiscordAuth(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)
        val userIdentification = session.getUserIdentification(loritta.config.loritta.discord.applicationId.toLong(), loritta.config.loritta.discord.clientSecret, call)

        var multipartFileBody: ByteArray? = null
        val postParams: Parameters

        // Is this a multipart request?
        if (call.request.isMultipart()) {
            // Here's the thing: We don't know if it is a "curl request" only or not until we actually parse everything, so whatever...
            val multipart = call.receiveMultipart()
            val map = mutableMapOf<String, String>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        map[part.name!!] = part.value
                    }
                    is PartData.FileItem -> {
                        // Handle file part if necessary
                        multipartFileBody = part.streamProvider().use {
                            if (it.available() >= 8_388_608) // 8mib
                                error("Uploaded file too big!")

                            it.readBytes()
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            // Fallback if the user doesn't upload anything
            if (multipartFileBody == null)
                multipartFileBody = ByteArray(0)

            postParams = Parameters.build {
                map.forEach { (key, value) ->
                    append(key, value)
                }
            }
        } else {
            multipartFileBody = null
            postParams = call.receiveParameters()
        }

        val endpointId = postParams.getOrFail("endpointId")
        val executeRequest = postParams.getOrFail("executeRequest")
            .toBoolean()

        val endpoint = LoriPublicHttpApiEndpoints::class.declaredMembers
            .first { it.name == endpointId }
            .call(LoriPublicHttpApiEndpoints) as LoriPublicHttpApiEndpoint

        val endpointTesterOptions = MagicEndpoints.endpointTesterOptions[endpoint] ?: error("Whoops")

        if (endpointTesterOptions.isFullRequestBody && multipartFileBody == null)
            error("Missing file body on a isFullRequestBody request!")
        if (!endpointTesterOptions.isFullRequestBody && multipartFileBody != null)
            error("File body present on a request that isn't isFullRequestBody!")

        var pathToBeUsed = endpoint.path

        for (pathParameter in endpointTesterOptions.pathParameters) {
            pathToBeUsed = pathToBeUsed.replace("{${pathParameter.name}}", postParams.getOrFail("pathparameter:${pathParameter.name}"))
        }

        val requestBody: Any? = if (endpointTesterOptions.isFullRequestBody) {
            // Small optimization: If we aren't planning to execute the request for realsies, just use a zero bytes ByteArray
            multipartFileBody
        } else if (endpointTesterOptions.jsonBodyBuilder != null) {
            endpointTesterOptions.jsonBodyBuilder.invoke(
                call,
                postParams
            )
        } else null

        val s = System.currentTimeMillis()
        val requestUrl = URLBuilder("https://api.loritta.website/v1$pathToBeUsed")
            .apply {
                for (queryParameter in endpointTesterOptions.queryParameters) {
                    val value = postParams.getAll("queryparameter:${queryParameter.name}")
                    if (value.isNullOrEmpty())
                        continue

                    val valueAsString = value.joinToString(",")
                    if (valueAsString.isBlank())
                        continue

                    parameters[queryParameter.name] = value.joinToString(",")
                }
            }.buildString()

        val authToken = postParams.getOrFail("authToken")
        val curlRequestCLI = buildString {
            append("curl \"$requestUrl\" ")
            if (endpoint.method != HttpMethod.Get)
                append("-X ${endpoint.method.value} ")
            when (requestBody) {
                is String -> append("--data '$requestBody' ")
                is ByteArray -> append("--data-binary @image.png ")
            }
            append("--header \"Authorization: lorixp_xxx\" -i")
        }

        // If we shouldn't execute the request, let's just show the curl command
        if (!executeRequest) {
            call.response.headerHXTrigger {
                playSoundEffect = "config-saved"
            }

            call.respondHtml(
                createHTML()
                    .body {
                        div {
                            span {
                                style = "color: #b0eb93;"
                                text("${userIdentification?.username ?: "wumpus"}@loritta:~# ")
                            }

                            span {
                                text(curlRequestCLI)
                            }
                        }
                    }
            )
            return
        }

        val response = http.request(requestUrl) {
            this.method = endpoint.method
            header("Authorization", authToken)
            userAgent("${loritta.lorittaCluster.getUserAgent(loritta)} EndpointTester (User ${userIdentification?.id})")
            if (requestBody != null) {
                when (requestBody) {
                    is String -> setBody(
                        TextContent(
                            requestBody,
                            ContentType.Application.Json
                        )
                    )
                    is ByteArray -> setBody(
                        ByteArrayContent(
                            requestBody,
                            ContentType.Image.PNG
                        )
                    )
                }
            }
        }

        val responseContentType = response.contentType()
        val responseBody = when (responseContentType) {
            ContentType.Image.PNG -> {
                APIResponseKind.ImageContent(response.readBytes())
            }
            ContentType.Application.Json -> {
                val bodyAsText = response.bodyAsText(Charsets.UTF_8)
                try {
                    val originalContent = Json.parseToJsonElement(bodyAsText)
                    APIResponseKind.TextContent(
                        Json {
                            prettyPrint = true
                        }.encodeToString(originalContent)
                    )
                } catch (e: SerializationException) {
                    // If something goes wrong when deserializing, fallback to the original input
                    logger.warn(e) { "Something went wrong while trying to deserialize the input!" }
                    APIResponseKind.TextContent(bodyAsText)
                }
            }
            else -> {
                APIResponseKind.TextContent(response.bodyAsText(Charsets.UTF_8))
            }
        }

        // Palette inspired by https://lospec.com/palette-list/vanilla-milkshake
        call.response.headerHXTrigger {
            playSoundEffect = "config-saved"
        }

        call.respondHtml(
            createHTML()
                .body {
                    div {
                        span(classes = "term-green") {
                            style = "color: #b0eb93;"
                            text("${userIdentification?.username ?: "wumpus"}@loritta:~# ")
                        }

                        span {
                            text(curlRequestCLI)
                        }
                    }

                    div(classes = "term-pink") {
                        text(response.version.name)
                        text(" ")
                        text(response.status.value)
                        text(" (")
                        text(response.status.description)
                        text(") [${System.currentTimeMillis() - s}ms]")
                    }

                    for (headerToBeShown in headersToBeShown) {
                        val headerFromResponse = response.headers[headerToBeShown]
                        if (headerFromResponse != null) {
                            div {
                                span("term-blue") {
                                    text("$headerToBeShown: ")
                                }

                                text(headerFromResponse)
                            }
                        }
                    }

                    div(classes = "term-orange") {
                        style = "white-space: pre-wrap;"

                        when (responseBody) {
                            is APIResponseKind.ImageContent -> {
                                img(src = "data:image/png;base64,${Base64.getEncoder().encodeToString(responseBody.imageData)}") {
                                    style = "width: 100%; height: auto;"
                                }
                            }
                            is APIResponseKind.TextContent -> {
                                text(responseBody.content)
                            }
                        }
                    }
                }
        )
    }

    sealed class APIResponseKind {
        class TextContent(val content: String) : APIResponseKind()
        class ImageContent(val imageData: ByteArray) : APIResponseKind()
    }
}