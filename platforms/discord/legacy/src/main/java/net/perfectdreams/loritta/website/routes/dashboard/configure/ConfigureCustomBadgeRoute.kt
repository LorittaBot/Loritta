package net.perfectdreams.loritta.website.routes.dashboard.configure

import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class ConfigureCustomBadgeRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/badge", "badge", "configure_badge.html")