package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureCustomCommandsRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/custom-commands", "custom_commands", "configure_custom_commands.html")