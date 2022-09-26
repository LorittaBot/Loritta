package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureEconomyRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/economy", "economy", "configure_economy.html")