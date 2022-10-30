package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.LorittaBot

class ConfigureCustomCommandsRoute(loritta: LorittaBot) : GenericConfigurationRoute(
    loritta,
    "/configure/custom-commands",
    "custom_commands",
    "configure_custom_commands.html"
)