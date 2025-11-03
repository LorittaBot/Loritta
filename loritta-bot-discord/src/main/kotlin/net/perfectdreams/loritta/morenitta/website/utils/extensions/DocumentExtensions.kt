package net.perfectdreams.loritta.morenitta.website.utils.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.google.gson.JsonElement
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.gson
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils

suspend fun ApplicationCall.respondJson(json: JsonElement, status: HttpStatusCode? = null) = this.respondText(ContentType.Application.Json, status) {
	gson.toJson(json)
}

suspend fun ApplicationCall.respondJson(json: JsonNode, status: HttpStatusCode? = null) = this.respondText(ContentType.Application.Json, status) {
	Constants.JSON_MAPPER.writeValueAsString(json)
}

suspend fun ApplicationCall.respondJson(json: kotlinx.serialization.json.JsonElement, status: HttpStatusCode? = null) = this.respondText(ContentType.Application.Json, status) {
	json.toString()
}

suspend fun ApplicationCall.respondJson(json: String, status: HttpStatusCode? = null) = this.respondText(ContentType.Application.Json, status) {
	json
}

suspend fun ApplicationCall.respondHtml(html: String, status: HttpStatusCode? = null) = this.respondText(ContentType.Text.Html, status) { html }

/**
 * Returns the request "true IP"
 * If the "X-Forwarded-For" header is set, then the value of that header is used, if not, Jooby's [Request.ip()] is used
 */
val ApplicationRequest.trueIp: String get() {
	val forwardedForHeader = this.header("X-Forwarded-For")
	return forwardedForHeader?.split(",")?.map { it.trim() }?.first() ?: this.local.remoteHost
		.let {
			// TODO: When Ktor is updated to 2.2.0 this won't be needed: https://github.com/ktorio/ktor/pull/3122
			if (it == "kubernetes.docker.internal")
				"127.0.0.1"
			else
				it
		}
}

/**
 * Returns the query strings as used in URLs (prefixed with "?")
 */
val ApplicationRequest.urlQueryString: String get() {
	val originalQueryString = this.queryString()

	return if (originalQueryString.isNotEmpty()) {
		"?$originalQueryString"
	} else {
		""
	}
}

/**
 * Returns the host from the "Host" header, if the header is missing, defaults to [ApplicationRequest.host]
 *
 * This is used when we need to get the user's host port too, due to [ApplicationRequest.host] not returing the host + port
 */
fun ApplicationRequest.hostFromHeader() = this.header("Host") ?: this.host()

class HttpRedirectException(val location: String, val permanent: Boolean = false) : RuntimeException()
fun redirect(location: String, permanent: Boolean = false): Nothing = throw HttpRedirectException(location, permanent)

var ApplicationCall.alreadyHandledStatus: Boolean
	get() = this.attributes.getOrNull(WebsiteUtils.handledStatusBefore) ?: false
	set(value) = this.attributes.put(WebsiteUtils.handledStatusBefore, value)