package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureDailyMultiplierRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/daily-multiplier", "daily_multiplier", "configure_daily_multiplier.html")