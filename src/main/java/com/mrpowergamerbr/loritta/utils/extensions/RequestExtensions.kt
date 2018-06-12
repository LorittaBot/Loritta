package com.mrpowergamerbr.loritta.utils.extensions

import org.jooby.Request

/**
 * Returns the request "true IP"
 * If the "X-Forwarded-For" header is set, then the value of that header is used, if not, Jooby's [Request.ip()] is used
 */
val Request.trueIp: String get() {
	val forwardedForHeader = this.header("X-Forwarded-For")
	return if (forwardedForHeader.isSet)
		forwardedForHeader.value()
	else
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