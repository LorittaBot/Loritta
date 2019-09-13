package net.perfectdreams.loritta.utils.locale

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.io.File

class LocaleLoader(val root: File) {
	/**
	 * Initializes the [id] locale and adds missing translation strings to non-default languages
	 *
	 * @see BaseLocale
	 */
	fun loadLocale(id: String, defaultLocale: BaseLocale?): BaseLocale {
		val locale = BaseLocale(id)
		if (defaultLocale != null) {
			// Colocar todos os valores padrões
			locale.localeEntries.putAll(defaultLocale.localeEntries)
		}

		val localeFolder = File(root, id)

		if (localeFolder.exists()) {
			localeFolder.listFiles().filter { it.extension == "yml" }.forEach {
				val entries = Constants.YAML.load<MutableMap<String, Any?>>(it.readText())

				fun transformIntoFlatMap(map: MutableMap<String, Any?>, prefix: String) {
					map.forEach { (key, value) ->
						if (value is Map<*, *>) {
							transformIntoFlatMap(value as MutableMap<String, Any?>, "$prefix$key.")
						} else {
							if (locale.localeEntries[prefix + key] != null)
								println("Duplicate key found! ${prefix + key} in $it")

							if (locale.localeEntries[prefix + key] == null)
								println("Unknown key found! ${prefix + key} in $it")
							locale.localeEntries[prefix + key] = value
						}
					}
				}

				transformIntoFlatMap(entries, "")
			}
		}

		return locale
	}

	/**
	 * Initializes the available locales and adds missing translation strings to non-default languages
	 *
	 * @see BaseLocale
	 */
	fun loadLocales(): Map<String, BaseLocale> {
		val locales = mutableMapOf<String, BaseLocale>()

		val defaultLocale = loadLocale(Constants.DEFAULT_LOCALE_ID, null)
		locales[Constants.DEFAULT_LOCALE_ID] = defaultLocale

		val localeFolder = root
		localeFolder.listFiles().filter { it.isDirectory && it.name != Constants.DEFAULT_LOCALE_ID && !it.name.startsWith(".") /* ignorar .git */ } .forEach {
			locales[it.name] = loadLocale(it.name, defaultLocale)
		}

		return locales
	}

	fun loadKeysFromFile(f: File): Map<String, Any?> {
		val localeKeys = mutableMapOf<String, Any?>()

		val entries = Constants.YAML.load<MutableMap<String, Any?>>(f.readText())

		fun transformIntoFlatMap(map: MutableMap<String, Any?>, prefix: String) {
			map.forEach { (key, value) ->
				if (value is Map<*, *>) {
					transformIntoFlatMap(value as MutableMap<String, Any?>, "$prefix$key.")
				} else {
					localeKeys[prefix + key] = value
				}
			}
		}

		transformIntoFlatMap(entries, "")

		return localeKeys
	}

	fun rebuildToMaps(localeKeys: Map<String, Any?>): Map<String, Any?> {
		val map = mutableMapOf<String, Any?>()

		for ((key, value) in localeKeys) {
			val split = key.split(".")
			val splitWithoutLast = split.dropLast(1)

			var endMap = map
			for (section in splitWithoutLast) {
				val t = endMap[section]

				if (t == null) {
					val newMap = mutableMapOf<String, Any?>()
					endMap[section] = newMap
					endMap = newMap
				} else if (t is Map<*, *>) {
					endMap = t as MutableMap<String, Any?>
				}
			}

			endMap[split.last()] = localeKeys[key]
		}

		return map
	}
}