package net.perfectdreams.loritta.website.routes.dashboard.configure

import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class ConfigureTwitchRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/twitch", "twitch", "configure_twitch.html")