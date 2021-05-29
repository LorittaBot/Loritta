package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

object FeatureFlags {
	val CINNAMON_COMMAND_API: Boolean
		get() = isEnabled(Names.CINNAMON_COMMAND_API)

	fun isEnabled(name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}

	fun isEnabled(loritta: LorittaDiscord, name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}

	object Names {
		const val CINNAMON_COMMAND_API = "cinnamon-command-api"
	}
}