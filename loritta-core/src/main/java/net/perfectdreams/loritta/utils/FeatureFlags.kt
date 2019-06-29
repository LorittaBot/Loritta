package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.loritta

object FeatureFlags {
	const val NEW_WEBSITE_PORT = "new-website-port"

	fun isEnabled(name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}
}