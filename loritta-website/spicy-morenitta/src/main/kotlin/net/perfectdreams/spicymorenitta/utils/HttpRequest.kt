package net.perfectdreams.spicymorenitta.utils

import org.w3c.xhr.XMLHttpRequest
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HttpRequest {
	companion object {
		suspend fun get(url: String): HttpResponse {
			val xmlHttp = XMLHttpRequest()
			xmlHttp.open("GET", url, true) // true for asynchronous
			return suspendCoroutine { cont ->
				xmlHttp.onreadystatechange = {
					if (xmlHttp.readyState == 4.toShort()) {
						cont.resume(
								HttpResponse(
										xmlHttp.status.toInt(),
										xmlHttp.responseText
								)
						)
					}
				}
				xmlHttp.send(null)
			}
		}

		suspend fun post(url: String, data: String): HttpResponse {
			val xmlHttp = XMLHttpRequest()
			xmlHttp.open("POST", url, true) // true for asynchronous
			return suspendCoroutine { cont ->
				xmlHttp.onreadystatechange = {
					if (xmlHttp.readyState == 4.toShort()) {
						cont.resume(
								HttpResponse(
										xmlHttp.status.toInt(),
										xmlHttp.responseText
								)
						)
					}
				}
				xmlHttp.send(data)
			}
		}

		suspend fun patch(url: String, data: String): HttpResponse {
			val xmlHttp = XMLHttpRequest()
			xmlHttp.open("PATCH", url, true) // true for asynchronous
			return suspendCoroutine { cont ->
				xmlHttp.onreadystatechange = {
					if (xmlHttp.readyState == 4.toShort()) {
						cont.resume(
								HttpResponse(
										xmlHttp.status.toInt(),
										xmlHttp.responseText
								)
						)
					}
				}
				xmlHttp.send(data)
			}
		}
	}
}