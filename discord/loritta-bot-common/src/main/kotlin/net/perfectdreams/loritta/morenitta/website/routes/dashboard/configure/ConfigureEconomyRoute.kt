package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureEconomyRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/economy", "economy", "configure_economy.html")