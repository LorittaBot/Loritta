package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigurePremiumKeyRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/premium", "premium", "configure_premium.html")