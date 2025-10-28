package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.respondText
import kotlinx.html.BODY
import kotlinx.html.HTML
import kotlinx.html.TagConsumer
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