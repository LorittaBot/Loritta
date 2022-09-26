package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureMiscellaneousRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/miscellaneous", "miscellaneous", "configure_miscellaneous.html") {
	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	class FakeServerConfig(val miscellaneousConfig: FakeMiscellaneousConfig) {
		class FakeMiscellaneousConfig(
				val enableBomDiaECia: Boolean,
				val enableQuirky: Boolean
		)
	}
}