package com.mrpowergamerbr.loritta.utils.extensions

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.utils.gson
import org.jooby.Mutant
import org.jooby.Request
import org.jooby.Response

/**
 * Returns the request "true IP"
 * If the "X-Forwarded-For" header is set, then the value of that header is used, if not, Jooby's [Request.ip()] is used
 */
val Request.trueIp: String get() {
	val forwardedForHeader = this.header("X-Forwarded-For")
	return if (forwardedForHeader.isSet) {
		forwardedForHeader.value().split(", ").first()
	} else
		this.ip()
}

/**
 * Returns the query strings as used in URLs (prefixed with "?")
 */
val Request.urlQueryString: String get() {
	return if (this.queryString().isPresent) {
		"?" + this.queryString().get()
	} else {
		""
	}
}

/**
 * Returns the value of the mutant, or null, if missing
 */
fun Mutant.valueOrNull(): String? {
	return if (this.isSet)
		this.value()
	else
		null
}

fun Response.send(element: JsonElement) {
	this.send(gson.toJsonTree(element))
}