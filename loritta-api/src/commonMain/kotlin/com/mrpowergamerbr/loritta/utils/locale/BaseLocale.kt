package com.mrpowergamerbr.loritta.utils.locale

import mu.KotlinLogging
import net.perfectdreams.loritta.api.utils.format

class BaseLocale(val id: String) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val localeEntries = mutableMapOf<String, Any?>()
	val path: String
		get() = this["website.localePath"]

	operator fun get(key: String, vararg arguments: Any?): String {
		try {
			return getWithType<String>(key).format(*arguments)
		} catch (e: RuntimeException) {
			logger.error(e) { "Error when trying to retrieve $key for locale $id" }
		}
		return "!!{$key}!!"
	}

	fun <T> getWithType(key: String): T {
		val entry = localeEntries[key] ?: throw RuntimeException("Key $key doesn't exist in locale $id!")
		return entry as T
	}
}