package com.mrpowergamerbr.loritta.utils.webpaste

import com.github.kevinsawicki.http.HttpRequest
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class TemmieBitly(internal var apiKey: String, internal var login: String) {

	fun shorten(longUrl: String): String? {
		var body: String? = null
		try {
			body = HttpRequest.get(String.format("https://api-ssl.bitly.com/v3/shorten?format=txt&apiKey=%s&login=%s&longUrl=%s", apiKey, login, URLEncoder.encode(longUrl, "UTF-8"))).body()
		} catch (e: HttpRequestException) {
			e.printStackTrace()
		} catch (e: UnsupportedEncodingException) {
			e.printStackTrace()
		}

		return body
	}

	fun shorten(longUrl: String, customUrl: String): String? {
		var body: String? = null
		try {
			body = HttpRequest.get(String.format("https://api-ssl.bitly.com/v3/shorten?format=txt&apiKey=%s&login=%s&longUrl=%s&domain=%s", apiKey, login, URLEncoder.encode(longUrl, "UTF-8"), URLEncoder.encode(customUrl, "UTF-8"))).body()
		} catch (e: HttpRequestException) {
			e.printStackTrace()
		} catch (e: UnsupportedEncodingException) {
			e.printStackTrace()
		}

		return body
	}

	fun expand(shortUrl: String): String? {
		return HttpRequest.get(String.format("https://api-ssl.bitly.com/v3/expand?format=txt&apiKey=%s&login=%s&shortUrl=%s", apiKey, login, URLEncoder.encode(shortUrl, "UTF-8"))).body()
	}
}