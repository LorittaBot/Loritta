package net.perfectdreams.loritta.website.frontend

import kotlinx.browser.document
import kotlin.js.Date

object CookieUtils {
    fun createCookie(name: String, value: String, days: Int? = null) {
        val expires = if (days != null) {
            "; expires=${(Date(Date.now() + (days * 86400) * 1000)).toUTCString()}"
        } else ""

        document.cookie = "$name=$value$expires; Path=/";
    }

    // https://stackoverflow.com/a/15724300
    fun readCookie(name: String): String? {
        val value = "; " + document.cookie
        val parts = value.split("; $name=")
        if (parts.size == 2)
            return parts.last().split(";").first()
        return null
    }

    // https://stackoverflow.com/a/2138471
    fun eraseCookie(name: String) {
        document.cookie = "$name=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;"
    }
}