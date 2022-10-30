package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.LorittaBot

class ConfigureCustomBadgeRoute(loritta: LorittaBot) :
    GenericConfigurationRoute(loritta, "/configure/badge", "badge", "configure_badge.html")