package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.LorittaBot

object FeatureFlags {
	fun isEnabled(name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}

	fun isEnabled(loritta: LorittaBot, name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}
}