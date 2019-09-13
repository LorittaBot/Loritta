package net.perfectdreams.loritta.utils.locale

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.random.Random

class FurryTranslation(val defaultFolder: File, val localeFolder: File, val localeLoader: LocaleLoader) {
	fun translate() {
		defaultFolder.listFiles().filter { it.extension == "yml" }.forEach {
			println(it)

			val def = localeLoader.loadKeysFromFile(it)
			val newBase = def.toMutableMap()

			def.forEach {
				val defaultValue = it.value

				if (defaultValue is String) {
					newBase[it.key] = furrify(defaultValue)
				} else if (defaultValue is List<*>) {
					val k = it.key

					val newTranslationList = mutableListOf<String>()

					defaultValue.forEach {
						newTranslationList.add(furrify(it as String))
					}

					newBase[k] = newTranslationList
				}
			}

			val rebuildToMap = localeLoader.rebuildToMaps(newBase)
			println(rebuildToMap)

			val options = DumperOptions()
			options.splitLines = false
			options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
			options.isPrettyFlow = true

			println(Yaml(options).dump(rebuildToMap))

			File(localeFolder, "pt-furry\\${it.name}").writeText(Yaml(options).dump(rebuildToMap))
		}
	}


	val replacements = mapOf(
			"r" to "w",
			"l" to "w",
			"R" to "W",
			"L" to "W",
			"ow" to "OwO",
			"no" to "nu",
			"has" to "haz",
			"have" to "haz",
			"you" to "uu",
			"the " to "da ",
			"fofo" to "foof",
			"fofa" to "foof",
			"ito" to "it",
			"dade" to "dad",
			"tando" to "tand",
			"ens" to "e",
			"tas" to "ts",
			"quanto" to "quant",
			"ente" to "ent"
	)

	val suffixes = listOf(
			":3",
			"UwU",
			"ʕʘ‿ʘʔ",
			">_>",
			"^_^",
			"^-^",
			";_;",
			";-;",
			"xD",
			"x3",
			":D",
			":P",
			";3",
			"XDDD",
			"ㅇㅅㅇ",
			"(人◕ω◕)",
			"（＾ｖ＾）",
			">_<"
	)

	fun furrify(input: String): String {
		var new = input

		val suffix = if (new.length % 4 == 0) {
			when {
				new.contains("triste", true) || new.contains("desculp", true) || new.contains("sorry", true) -> ">_<"
				new.contains("parabéns", true) -> "(人◕ω◕)"
				else -> suffixes.random(Random(new.hashCode()))
			}
		} else ""

		for ((from, to) in replacements) {
			new = new.replace(from, to)
		}

		new += " $suffix"
		new = new.trim()

		return new
	}
}