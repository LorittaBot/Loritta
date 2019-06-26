package net.perfectdreams.loritta.website

import com.mrpowergamerbr.loritta.utils.loritta
import java.io.File

/**
 * Clone of the original "LorittaWebsite" from the "sweet-morenitta" module
 *
 * This is used as a "hack" until the new website is done
 */
class LorittaWebsite {
	companion object {
		lateinit var INSTANCE: LorittaWebsite
		val versionPrefix = "/v2"

		// Hack!
		fun init() {
			if (!::INSTANCE.isInitialized)
				INSTANCE = LorittaWebsite()
		}
	}

	val pathCache = mutableMapOf<File, Any>()
	var config = WebsiteConfig()

	class WebsiteConfig {
		val websiteUrl: String
				get() = loritta.config.loritta.website.url.removeSuffix("/")
		val websiteFolder = File(loritta.config.loritta.folders.root, "website")
	}
}