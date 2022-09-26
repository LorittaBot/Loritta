package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord

class ConfigureCustomCommandsRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/custom-commands", "custom_commands", "configure_custom_commands.html")