package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureTrackedTwitterAccountsRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/twitter", "twitter", "configure_twitter.html")