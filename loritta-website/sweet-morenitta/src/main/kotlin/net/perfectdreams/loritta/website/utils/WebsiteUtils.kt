package net.perfectdreams.loritta.website.utils

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.website.LorittaWebsite

val gson = Gson()
val jsonParser = JsonParser()
val website by lazy { LorittaWebsite.INSTANCE }

object WebsiteUtils {
    /**
     * Sends a JSON API error to the [call]
     *
     * @param call           the application call
     * @param code           lori's web code status
     * @param message        custom error message if needed
     * @param httpStatusCode status code
     */
    suspend fun sendApiError(call: ApplicationCall, code: LoriWebCode, message: String? = null, httpStatusCode: HttpStatusCode = HttpStatusCode.InternalServerError, data: ((ObjectNode) -> (ObjectNode))? = null) {
        call.respondText(ContentType.Application.Json, httpStatusCode) {
            Constants.JSON_MAPPER.writeValueAsString(
                createErrorObject(
                    code,
                    message,
                    data
                )
            )
        }
    }

    /**
     * Creates an JSON object containing the code error
     *
     * @param code    the error code
     * @param message the error reason
     * @return        the json object with the error
     */
    fun createErrorObject(code: LoriWebCode, message: String? = null, data: ((ObjectNode) -> (ObjectNode))? = null): ObjectNode {
        val objectNode = JsonNodeFactory.instance.objectNode()
            .put("code", code.errorId)
            .put("error", code.fancyName)
            .let {
                data?.invoke(it) ?: it
            }

        if (message != null)
            objectNode.put("message", message)

        return objectNode
    }

    fun getDiscordCrawlerAuthenticationPage(): String {
        return createHTML().html {
            head {
                fun setMetaProperty(property: String, content: String) {
                    meta(content = content) { attributes["property"] = property }
                }
                title("Login • Loritta")
                setMetaProperty("og:site_name", "Loritta")
                setMetaProperty("og:title", "Painel da Loritta")
                setMetaProperty("og:description", "Meu painel de configuração, aonde você pode me configurar para deixar o seu servidor único e incrível!")
                setMetaProperty("og:image", website.config.websiteUrl + "assets/img/loritta_dashboard.png")
                setMetaProperty("og:image:width", "320")
                setMetaProperty("og:ttl", "660")
                setMetaProperty("og:image:width", "320")
                setMetaProperty("theme-color", "#7289da")
                meta("twitter:card", "summary_large_image")
            }
            body {
                p {
                    + "Parabéns, você encontrou um easter egg!"
                }
            }
        }
    }
}