package net.perfectdreams.loritta.cinnamon.discord.utils.google

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * A Google Translate Client that depends on undocumented endpoints, hacky!
 *
 * https://github.com/ssut/py-googletrans/issues/268
 */
class HackyGoogleTranslateClient {
    val http = HttpClient(CIO)

    suspend fun translate(from: GoogleTranslateLanguage, to: GoogleTranslateLanguage, input: String) = translate(
        from.code,
        to.code,
        input
    )

    private suspend fun translate(from: String, to: String, input: String): GoogleTranslateResponse? {
        val a = http.get("https://translate.googleapis.com/translate_a/single") {
            parameter("client", "gtx")
            parameter("sl", from)
            parameter("tl", to)
            parameter("q", input)
            parameter("dt", "t")
            parameter("ie", "UTF-8")
            parameter("oe", "UTF-8")
            userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0")
        }.bodyAsText()

        // Example: [[["Olá Mundo! ","Hello World!",null,null,1],["Como você está?","How are you?",null,null,10]],null,"en",null,null,null,null,[]]
        val response = Json.parseToJsonElement(a)
            .jsonArray

        val firstElementOnTheArray = response[0]
        // Nothing was translated: Example, if you input ""
        if (firstElementOnTheArray is JsonNull)
            return null

        val detectedLanguage = GoogleTranslateLanguage.fromLanguageCode(response[2].jsonPrimitive.content)

        val output = StringBuilder()
        firstElementOnTheArray.jsonArray.forEach {
            val innerArray = it.jsonArray

            val translated = innerArray[0].jsonPrimitive.content
            val source = innerArray[1].jsonPrimitive.content

            output.append(translated)
        }

        return GoogleTranslateResponse(
            output.toString(),
            detectedLanguage
        )
    }

    data class GoogleTranslateResponse(
        val output: String,
        val sourceLanguage: GoogleTranslateLanguage
    )
}