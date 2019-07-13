package net.perfectdreams.spicymorenitta

import kotlin.browser.document

object CookiesUtils {
	fun createCookie(name: String, value: String) {
		val expires = ""
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