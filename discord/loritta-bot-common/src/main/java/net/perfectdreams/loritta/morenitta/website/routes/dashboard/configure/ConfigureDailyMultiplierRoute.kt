package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureDailyMultiplierRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/daily-multiplier", "daily_multiplier", "configure_daily_multiplier.html")