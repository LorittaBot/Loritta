package net.perfectdreams.loritta.legacy.utils

import net.perfectdreams.loritta.legacy.utils.loritta
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

object FeatureFlags {
	fun isEnabled(name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}

	fun isEnabled(loritta: LorittaDiscord, name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}
}