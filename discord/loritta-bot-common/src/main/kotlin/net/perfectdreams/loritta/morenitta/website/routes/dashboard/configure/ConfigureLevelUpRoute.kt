package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.LorittaBot

class ConfigureLevelUpRoute(loritta: LorittaBot) :
    GenericConfigurationRoute(loritta, "/configure/level", "level", "configure_level.html")