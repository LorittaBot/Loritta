package utils

import kotlin.js.Json

class LegacyBaseLocale {
	companion object {
		fun create(json: Json): LegacyBaseLocale {
			val map: MutableMap<String, String> = linkedMapOf()
			for (key in js("Object").keys(json)) {
				map.put(key, json[key] as String)
			}
			println("Keys: ${map.entries.size}")
			return LegacyBaseLocale().apply {
				strings = map
			}
		}
	}
	var strings = mapOf<String, String>()

	operator fun get(key: String, vararg arguments: Any?): String {
		// O jeitinho pobre de locales em Kotlin JS:tm:
		var string = strings[key] ?: return key

		for ((index, arg) in arguments.withIndex()) {
			string = string.replace("{$index}", arg.toString())
		}
		return string
	}
}