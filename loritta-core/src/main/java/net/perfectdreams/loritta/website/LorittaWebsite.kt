package net.perfectdreams.loritta.website

import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.website.blog.Blog
import java.io.File
import java.util.concurrent.ConcurrentHashMap

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

	val pathCache = ConcurrentHashMap<File, Any>()
	var config = WebsiteConfig()
	val blog = Blog()

	class WebsiteConfig {
		val websiteUrl: String
				get() = loritta.instanceConfig.loritta.website.url.removeSuffix("/")
		val websiteFolder = File(loritta.instanceConfig.loritta.folders.root, "website")
	}
}