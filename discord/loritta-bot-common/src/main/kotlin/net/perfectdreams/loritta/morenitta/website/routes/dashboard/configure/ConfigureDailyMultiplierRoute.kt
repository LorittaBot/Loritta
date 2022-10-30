package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.LorittaBot

class ConfigureDailyMultiplierRoute(loritta: LorittaBot) : GenericConfigurationRoute(
    loritta,
    "/configure/daily-multiplier",
    "daily_multiplier",
    "configure_daily_multiplier.html"
)