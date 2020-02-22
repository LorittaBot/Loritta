package net.perfectdreams.loritta.website.routes

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.routing.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

abstract class BaseRoute(val loritta: LorittaDiscord, val path: String) {
	abstract suspend fun onRequest(call: ApplicationCall)

	fun register(routing: Routing) {
		val method = getMethod()
		when (method) {
			HttpMethod.Get -> routing.get(path) { onRequest(call) }
			HttpMethod.Post -> routing.post(path) { onRequest(call) }
			HttpMethod.Patch -> routing.patch(path) { onRequest(call) }
			HttpMethod.Put -> routing.put(path) { onRequest(call) }
			HttpMethod.Delete -> routing.delete(path) { onRequest(call) }
			else -> routing.get(path) { onRequest(call) }
		}
	}

	open fun getMethod(): HttpMethod {
		val className = this::class.java.simpleName.toLowerCase()
		return when {
			className.startsWith("get") -> HttpMethod.Get
			className.startsWith("post") -> HttpMethod.Post
			className.startsWith("patch") -> HttpMethod.Patch
			className.startsWith("put") -> HttpMethod.Put
			className.startsWith("delete") -> HttpMethod.Delete
			else -> HttpMethod.Get
		}
	}
}