package net.perfectdreams.loritta.website.routes.dashboard.configure

import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class ConfigureCustomCommandsRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/custom-commands", "custom_commands", "configure_custom_commands.html")