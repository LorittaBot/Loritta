package net.perfectdreams.spicymorenitta.utils

import kotlin.js.Json

class JsonObject(backed: Json) : JsonElement(backed) {
	operator fun get(s: String): JsonElement {
		val json = backed as Json

		val result = backed[s]
		return JsonElement(result)
	}
}