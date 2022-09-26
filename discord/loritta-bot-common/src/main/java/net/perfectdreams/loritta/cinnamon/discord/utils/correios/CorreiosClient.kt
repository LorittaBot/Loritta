package net.perfectdreams.loritta.cinnamon.discord.utils.correios

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosResponse
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.exceptions.InvalidTrackingIdException
import net.perfectdreams.loritta.cinnamon.utils.JsonIgnoreUnknownKeys
import java.io.Closeable

class CorreiosClient : Closeable {
    companion object {
        val CORREIOS_PACKAGE_REGEX = Regex("[A-Z]{2}[0-9]{9}[A-Z]{2}")
    }

    val http = HttpClient {
        expectSuccess = false
    }

    /**
     * Gets package tracking information about the [trackingIds]
     *
     * @param trackingIds list of tracking IDs, Correios doesn't seem to limit how many packages you can track at the same time
     * @return the request response
     * @throws InvalidTrackingIdException if any of the [trackingIds] do not match the [CORREIOS_PACKAGE_REGEX] RegEx
     */
    suspend fun getPackageInfo(vararg trackingIds: String): CorreiosResponse {
        // Validate tracking IDs
        for (trackingId in trackingIds)
            if (!trackingId.matches(CORREIOS_PACKAGE_REGEX))
                throw InvalidTrackingIdException(trackingId)

        // Eu encontrei a API REST do Correios usando engenharia reversa(tm) no SRO Mobile
        val httpResponse = http.post("http://webservice.correios.com.br/service/rest/rastro/rastroMobile") {
            userAgent("Dalvik/2.1.0 (Linux; U; Android 7.1.2; MotoG3-TE Build/NJH47B)")
            accept(ContentType.Application.Json)

            // Não importa qual é o usuário/senha/token, ele sempre retorna algo válido
            setBody(
                TextContent(
                    "<rastroObjeto><usuario>LorittaBot</usuario><senha>LorittaSuperFofa</senha><tipo>L</tipo><resultado>T</resultado>${trackingIds.joinToString(prefix = "<objetos>", postfix = "</objetos>", separator = "")}<lingua>101</lingua><token>Loritta-Discord</token></rastroObjeto>",
                    ContentType.Application.Xml
                )
            )
        }

        val r = httpResponse.bodyAsText()
        return JsonIgnoreUnknownKeys.decodeFromString(r)
    }

    override fun close() {
        http.close()
    }
}