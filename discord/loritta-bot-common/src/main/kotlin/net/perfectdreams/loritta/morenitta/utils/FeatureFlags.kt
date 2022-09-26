package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

object FeatureFlags {
	fun isEnabled(name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}

	fun isEnabled(loritta: LorittaDiscord, name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}
}