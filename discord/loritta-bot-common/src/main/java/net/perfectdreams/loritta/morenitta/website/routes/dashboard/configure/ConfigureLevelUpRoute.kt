package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureLevelUpRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/level", "level", "configure_level.html")