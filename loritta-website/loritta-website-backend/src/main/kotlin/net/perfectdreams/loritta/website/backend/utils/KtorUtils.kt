package net.perfectdreams.loritta.website.backend.utils

import io.ktor.server.request.*
import net.perfectdreams.dokyo.WebsiteTheme

class HttpRedirectException(val location: String, val permanent: Boolean = false) : RuntimeException()
fun redirect(location: String, permanent: Boolean = false): Nothing = throw HttpRedirectException(location, permanent)

val ApplicationRequest.userTheme
    get() = cookies["userTheme"]?.let { WebsiteTheme.valueOf(it) } ?: WebsiteTheme.DEFAULT