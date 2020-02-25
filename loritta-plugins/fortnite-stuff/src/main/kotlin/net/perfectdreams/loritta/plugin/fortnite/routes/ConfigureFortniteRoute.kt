package net.perfectdreams.loritta.plugin.fortnite.routes

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.configure.GenericConfigurationRoute

class ConfigureFortniteRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/fortnite", "fortnite", "configure_fortnite.html")