package net.perfectdreams.loritta.utils.locale

import com.mrpowergamerbr.loritta.utils.translate.GoogleTranslateUtils
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.regex.Pattern

class AutoEnglishTranslation(val defaultFolder: File, val localeFolder: File, val localeLoader: LocaleLoader) {
	fun translate() {
		defaultFolder.listFiles().filter { it.extension == "yml" }.forEach {
			println(it)

			val ogEnglishFile = (File(localeFolder, "en-us/${it.name}"))
			val def = localeLoader.loadKeysFromFile(it)

			val ogEnglish = if (ogEnglishFile.exists()) {
				localeLoader.loadKeysFromFile(File(localeFolder, "en-us/${it.name}"))
			} else {
				def
			}

			val newBase = def.toMutableMap()

			def.forEach {
				val defaultValue = it.value

				if (ogEnglish[it.key] == null || ogEnglish[it.key] == defaultValue) {
					println("${it.value} (${defaultValue}) is not translated yet!")

					if (defaultValue is String) {
						val translated = translateToEnglish(defaultValue)

						val originalEmotes = extractEmotes(defaultValue)
						val translatedEmotes = extractEmotes(translated)

						if (!(originalEmotes.containsAll(translatedEmotes) && translatedEmotes.containsAll(originalEmotes))) {
							println("Emotes $originalEmotes -> $translatedEmotes are missing, ignoring translation...")
							newBase[it.key] = defaultValue
						} else {
							newBase[it.key] = "[===AutoTranslated!!!===]$translated"
						}
					} else if (defaultValue is List<*>) {
						val k = it.key

						val newTranslationList = mutableListOf<String>()

						defaultValue.forEach {
							val translated = translateToEnglish(k)

							val originalEmotes = extractEmotes(k)
							val translatedEmotes = extractEmotes(translated)

							if (!(originalEmotes.containsAll(translatedEmotes) && translatedEmotes.containsAll(originalEmotes))) {
								println("Emotes $originalEmotes -> $translatedEmotes are missing, ignoring translation...")
								newTranslationList.add(it as String)
							} else {
								newTranslationList.add("[===AutoTranslated!!!===]$translated")
							}
						}

						newBase[k] = newTranslationList
					}
					Thread.sleep(5_000) // sleep just to avoid 429
				} else {
					println("Already translated ${it.value} (${defaultValue} -> ${ogEnglish[it.key]}), skipping...")
				}
			}

			val rebuildToMap = localeLoader.rebuildToMaps(newBase)
			println(rebuildToMap)

			val options = DumperOptions()
			options.splitLines = false
			options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
			options.isPrettyFlow = true

			println(Yaml(options).dump(rebuildToMap))

			var asText = Yaml(options).dump(rebuildToMap)
			var lines = asText.lines()
			val newLines = mutableListOf<String>()

			for (line in lines) {
				if (line.contains("[===AutoTranslated!!!===]")) {
					var numberOfSpaces = 0
					for (ch in line) {
						if (ch.isWhitespace()) {
							numberOfSpaces++
						} else {
							break
						}
					}

					newLines.add("${" ".repeat(numberOfSpaces)}# This was autotranslated, if this translation is wrong, please fix and remove this comment! Thanks and have a nice day!")

					newLines.add(
							line.replace("[===AutoTranslated!!!===]", "")
					)
				} else {
					newLines.add(line)
				}
			}

			File(localeFolder, "en-us-auto\\${it.name}").writeText(newLines.joinToString("\n"))
		}
	}

	fun translateToEnglish(input: String): String {
		var r = GoogleTranslateUtils.translate(
				input,
				"auto",
				"en"
		)

		val newR = r
		r = newR!!.replace(" ...", "...")
				.replace("** {", "**{")
				.replace("} **", "}**")
				.replace("! **", "!**")
				.replace("? **", "?**")

		println(r)
		return r
	}

	fun extractEmotes(input: String): List<String> {
		val regex = Pattern.compile("(:[A-z0-9_]+:)")
				.toRegex()

		val wow = regex.findAll(input)
		return wow.map { it.value }.toList()
	}
}