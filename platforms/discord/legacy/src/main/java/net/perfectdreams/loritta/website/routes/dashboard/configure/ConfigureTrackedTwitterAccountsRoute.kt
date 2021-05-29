package net.perfectdreams.loritta.website.routes.dashboard.configure

import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class ConfigureTrackedTwitterAccountsRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/twitter", "twitter", "configure_twitter.html")