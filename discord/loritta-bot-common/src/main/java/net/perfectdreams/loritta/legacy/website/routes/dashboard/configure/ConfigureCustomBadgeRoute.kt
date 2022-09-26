package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureCustomBadgeRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/badge", "badge", "configure_badge.html")