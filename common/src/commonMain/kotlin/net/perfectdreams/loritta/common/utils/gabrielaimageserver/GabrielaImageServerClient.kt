package net.perfectdreams.loritta.common.utils.gabrielaimageserver

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.JsonObject

/**
 * Client for [GabrielaImageServer](https://github.com/LorittaBot/GabrielaImageGen]
 *
 * While the requests themselves are very simple, this is useful to wrap errors and exceptions.
 *
 * @param baseUrl the URL of the image server, example: https://gabriela.loritta.website
 * @param http    the http client that will be used for requests
 */
class GabrielaImageServerClient(baseUrl: String, val http: HttpClient) {
    val baseUrl = baseUrl.removeSuffix("/") // Remove trailing slash

    suspend fun execute(endpoint: String, body: JsonObject): ByteArray {
        val response = http.post<HttpResponse>("$baseUrl$endpoint") {
            this.body = body.toString()
        }

        // If the status code is between 400..499, then it means that it was (probably) a invalid input or something
        if (response.status.value in 400..499)
            throw NoValidImageFoundException()
        else if (response.status.value !in 200..299) // This should show the error message because it means that the server had a unknown error
            throw ErrorWhileGeneratingImageException()

        return response.receive()
    }
}