package com.mrpowergamerbr.loritta.utils.extensions

import org.jooby.Request

val Request.trueIp: String get() {
	val forwardedForHeader = this.header("X-Forwarded-For")
	return if (forwardedForHeader.isSet)
		forwardedForHeader.value()
	else
		this.ip()
}

val Request.urlQueryString: String get() {
	return if (this.queryString().isPresent) {
		"?" + this.queryString().get()
	} else {
		""
	}
}