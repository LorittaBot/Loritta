package net.perfectdreams.loritta.website.routes.dashboard.configure

import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class ConfigureLevelUpRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/level", "level", "configure_level.html")