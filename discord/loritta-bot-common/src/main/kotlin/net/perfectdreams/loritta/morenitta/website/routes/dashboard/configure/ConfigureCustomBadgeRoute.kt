package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureCustomBadgeRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/badge", "badge", "configure_badge.html")