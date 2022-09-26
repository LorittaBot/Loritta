package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureTrackedTwitterAccountsRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/twitter", "twitter", "configure_twitter.html")