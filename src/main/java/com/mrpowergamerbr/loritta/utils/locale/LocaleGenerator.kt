package com.mrpowergamerbr.loritta.utils.locale

import org.yaml.snakeyaml.Yaml
import java.io.File

fun main(args: Array<String>) {
	val yaml = Yaml();
	val obj = yaml.load(File("./default.yml").readText()) as Map<String, Object>
	System.out.println(obj)

	var classes = ""

	fun String.yamlToVariable(): String {
		var newVariable = ""
		var nextShouldBeUppercase = false
		for (ch in this) {
			if (ch == '-') {
				nextShouldBeUppercase = true
				continue
			}
			var thisChar = ch
			if (nextShouldBeUppercase)
				thisChar = thisChar.toUpperCase()
			newVariable += thisChar
			nextShouldBeUppercase = false
		}
		return newVariable
	}

	fun handle(name: String, entries: Map<*, *>, isRoot: Boolean, tabs: Int) {
		entries as Map<String, Any>

		classes += "class ${name.capitalize()} {\n"
		for ((key, value) in entries) {
			when {
				value is Map<*, *> -> {
					handle(key, value, true, tabs + 1)
					classes += "var ${key.toLowerCase()} = ${key.capitalize()}()\n\n"
				}
				value is List<*> -> {
					classes += "    lateinit var ${key.yamlToVariable()}: List<String>\n"
				}
				else -> {
					classes += "    lateinit var ${key.yamlToVariable()}: String\n"
				}
			}
		}
		classes += "}\n"
	}

	for ((key, value) in obj) {
		if (value is Map<*, *>)
			handle(key, value, true, 0)
		else {
			println("Invalid!")
		}
	}

	println(classes)
}