package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureTwitchRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/twitch", "twitch", "configure_twitch.html")