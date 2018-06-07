package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.keys
import com.github.salomonbrys.kotson.obj
import java.io.File

fun main(args: Array<String>) {
	val default = jsonParser.parse(File("C:\\Users\\Whistler\\AppData\\Local\\Temp\\fz3temp-2\\default.json").readText()).obj

	val used = mutableSetOf<String>()

	default.entrySet().forEach {
		println("Checking ${it.key}...")

		val key = it.key

		// find key magically
		fun findRecursive(file: File) {
			for (it in file.listFiles()) {
				if (it.isDirectory) {
					findRecursive(it)
				} else {
					val content = it.readText()

					if (content.contains("${key}")) {
						used.add(key)
					}
				}
			}
		}

		findRecursive(File("C:\\Users\\Whistler\\Documents\\IdeaProjects\\Loritta\\LorittaBot\\src\\main\\java\\com\\mrpowergamerbr\\loritta"))
	}

	val unused = default.keys().filterNot { used.contains(it) }

	println("UNUSED TRANSLATION KEYS:")
	unused.forEach {
		println(it)
	}
}