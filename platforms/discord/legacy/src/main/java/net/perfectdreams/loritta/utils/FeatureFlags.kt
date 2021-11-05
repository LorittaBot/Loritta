package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

object FeatureFlags {
	fun isEnabled(name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}

	fun isEnabled(loritta: LorittaDiscord, name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}
}