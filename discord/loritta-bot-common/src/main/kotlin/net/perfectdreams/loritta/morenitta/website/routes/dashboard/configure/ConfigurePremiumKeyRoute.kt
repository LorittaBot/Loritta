package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigurePremiumKeyRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/premium", "premium", "configure_premium.html")