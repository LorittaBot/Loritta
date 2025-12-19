package net.perfectdreams.dora.utils

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.ParametersBuilder
import io.ktor.http.formUrlEncode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import kotlinx.html.BODY
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.consumers.delayed
import kotlinx.html.consumers.onFinalizeMap
import kotlinx.html.html
import kotlinx.html.stream.HTMLStreamBuilder

// From kotlinx.html
private const val AVERAGE_PAGE_SIZE = 32768

suspend fun ApplicationCall.respondHtml(status: HttpStatusCode? = null, content: HTML.() -> (Unit)) {
    val output = StringBuilder(AVERAGE_PAGE_SIZE)
    output.append("<!doctype html>")

    val builder = HTMLStreamBuilder(
        output,
        prettyPrint = false,
        xhtmlCompatible = false
    ).onFinalizeMap { sb, _ -> sb.toString() }.delayed()

    builder.html {
        content.invoke(this)
    }

    this.respondText(
        output.toString(),
        ContentType.Text.Html,
        status = status
    )
}

suspend fun ApplicationCall.respondHtmlFragment(status: HttpStatusCode? = null, content: BODY.() -> (Unit)) {
    val output = StringBuilder(AVERAGE_PAGE_SIZE)

    val builder = HTMLStreamBuilder(
        output,
        prettyPrint = false,
        xhtmlCompatible = false
    ).onFinalizeMap { sb, _ -> sb.toString() }.delayed()

    builder.body {
        content.invoke(this)
    }

    this.respondText(
        output.toString(),
        ContentType.Text.Html,
        status = status
    )
}

fun PathBuilder(path: String, queryParameters: ParametersBuilder.() -> (Unit)): String {
    val query = Parameters.build {
        queryParameters()
    }.formUrlEncode() // safely URL-encodes keys/values

    return buildString {
        append(path)
        if (query.isNotEmpty()) {
            append('?')
            append(query)
        }
    }
}
