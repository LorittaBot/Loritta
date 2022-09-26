package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureTwitchRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/twitch", "twitch", "configure_twitch.html")