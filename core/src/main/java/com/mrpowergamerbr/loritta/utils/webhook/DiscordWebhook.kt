package com.mrpowergamerbr.loritta.utils.webhook

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.contains
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.utils.jsonParser
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class DiscordWebhook(val url: String) {
	var requestQueue = mutableListOf<Pair<DiscordMessage, ((JsonObject) -> Unit)?>>()
	var isRateLimited = false
	var cachedThreadPool = Executors.newCachedThreadPool()

	fun send(message: DiscordMessage, wait: Boolean = false, callback: ((JsonObject) -> Unit)? = null) {
		if (isRateLimited) {
			requestQueue.add(Pair(message, callback))
			return
		}
		cachedThreadPool.submit {
			var url = url
			if (wait) {
				url += "?wait=true"
			}
			val response = HttpRequest.post(url)
					.acceptJson()
					.contentType("application/json")
					.header("User-Agent", "Mozilla/5.0 (X11; U; Linux i686) Gecko/20071127 Firefox/2.0.0.11") // Why? Because discordapp.com blocks the default User Agent
					.send(GSON.toJson(message))
					.body()

			if (response.isNotEmpty()) { // oh no
				val json = jsonParser.parse(response).obj

				if (json.contains("retry_after")) { // Rate limited, vamos colocar o request dentro de uma queue
					requestQueue.add(Pair(message, callback))
					isRateLimited = true
					thread {
						Thread.sleep(json["retry_after"].long)
						isRateLimited = false

						val requests = requestQueue.toMutableList()
						requestQueue.clear()
						for (queue in requests) {
							send(queue.first, wait, queue.second)
						}
					}
				} else {
					if (callback != null) {
						callback.invoke(json)
					}
				}
			}
		}
	}
}