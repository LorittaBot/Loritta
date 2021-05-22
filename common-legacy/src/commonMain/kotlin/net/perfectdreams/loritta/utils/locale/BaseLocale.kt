package net.perfectdreams.loritta.utils.locale

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import net.perfectdreams.loritta.api.utils.format

@Serializable
class BaseLocale(val id: String) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val localeStringEntries = mutableMapOf<String, String?>()
	val localeListEntries = mutableMapOf<String, List<String>?>()
	val path: String
		get() = this["website.localePath"]

	operator fun get(localeKeyData: LocaleKeyData): String {
		// The spread operator is used in a .get(...) because it doesn't work inside of a [...], I don't know why
		val arguments = localeKeyData.arguments?.map {
			when (it) {
				is LocaleStringData -> it.text
				is LocaleKeyData -> get(it)
				else -> it.toString()
			}
		}?.toTypedArray() ?: arrayOf()

		return get(
				localeKeyData.key,
				*arguments
		)
	}

	fun getList(localeKeyData: LocaleKeyData): List<String> {
		val arguments = localeKeyData.arguments?.map {
			when (it) {
				is LocaleStringData -> it.text
				is LocaleKeyData -> get(it)
				else -> it.toString()
			}
		}?.toTypedArray() ?: arrayOf()

		return getList(
				localeKeyData.key,
				arguments
		)
	}

	operator fun get(key: String, vararg arguments: Any?): String {
		try {
			return localeStringEntries[key]?.format(*arguments) ?: throw RuntimeException("Key $key doesn't exist in locale $id!")
		} catch (e: RuntimeException) {
			logger.error(e) { "Error when trying to retrieve $key for locale $id" }
		}
		return "!!{$key}!!"
	}

	fun getList(key: String, vararg arguments: Any?): List<String> {
		try {
			return localeListEntries[key]?.map { it.format(*arguments) } ?: throw RuntimeException("Key $key doesn't exist in locale $id!")
		} catch (e: RuntimeException) {
			logger.error(e) { "Error when trying to retrieve $key for locale $id" }
		}
		return listOf("!!{$key}!!")
	}
}