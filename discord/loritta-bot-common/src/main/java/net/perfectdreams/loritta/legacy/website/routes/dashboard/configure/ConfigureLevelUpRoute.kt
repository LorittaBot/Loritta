package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureLevelUpRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/level", "level", "configure_level.html")