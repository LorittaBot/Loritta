package net.perfectdreams.loritta.website.routes.dashboard.configure

import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class ConfigureEconomyRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/economy", "economy", "configure_economy.html")