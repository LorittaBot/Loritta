package net.perfectdreams.loritta.website.routes.dashboard.configure

import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class ConfigurePremiumKeyRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/premium", "premium", "configure_premium.html")