package com.mrpowergamerbr.loritta

import com.mrpowergamerbr.loritta.utils.config.EnvironmentType

/**
 * Clone of the original "LorittaLauncher" from the "loritta-core" module
 *
 * This is used as a "hack" until the new website is done, used to have compat between the old and the new website
 */
object LorittaLauncher {
	val loritta = Loritta()

	class Loritta {
		val discordConfig = DiscordConfig()
		val config = Config()

		class Config {
			val loritta = LorittaConfig()

			class LorittaConfig {
				val environment = EnvironmentType.CANARY
			}
		}

		class DiscordConfig {
			val discord = Discord()

			class Discord {
				val addBotUrl = "http://example.com/"
			}
		}
	}
}