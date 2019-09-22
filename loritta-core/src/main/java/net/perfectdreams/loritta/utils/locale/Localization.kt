package net.perfectdreams.loritta.utils.locale

import java.io.File

object Localization {
	@JvmStatic
	fun main(args: Array<String>) {
		val localeRootFolder = System.getProperty("localeRootFolder")

		println(localeRootFolder)

		val localeFolder = File(localeRootFolder)
		val defaultFolder = File(localeRootFolder, "default")

		val localeLoader = LocaleLoader(defaultFolder)

		if ("furry" in args) {
			FurryTranslation(defaultFolder, localeFolder, localeLoader).translate()
		}
		if ("autoenglish" in args) {
			AutoEnglishTranslation(defaultFolder, localeFolder, localeLoader).translate()
		}
	}
}