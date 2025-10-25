package net.perfectdreams.bliss

import web.http.DELETE
import web.http.GET
import web.http.HEAD
import web.http.OPTIONS
import web.http.PATCH
import web.http.POST
import web.http.PUT
import web.http.RequestMethod

// Based on Ktor's HttpMethod class
data class HttpMethod(val value: String) {
    companion object {
        val Get = HttpMethod("GET")
        val Post = HttpMethod("POST")
        val Put = HttpMethod("PUT")

        // https://tools.ietf.org/html/rfc5789
        val Patch = HttpMethod("PATCH")
        val Delete = HttpMethod("DELETE")
        val Head = HttpMethod("HEAD")
        val Options = HttpMethod("OPTIONS")
    }

    fun toRequestMethod(): RequestMethod {
        return when (this.value) {
            "GET" -> RequestMethod.GET
            "POST" -> RequestMethod.POST
            "PUT" -> RequestMethod.PUT
            "PATCH" -> RequestMethod.PATCH
            "DELETE" -> RequestMethod.DELETE
            "HEAD" -> RequestMethod.HEAD
            "OPTIONS" -> RequestMethod.OPTIONS
            else -> throw IllegalArgumentException("Invalid HTTP method: $value")
        }
    }
}