package net.perfectdreams.loritta.website.routes.dashboard.configure

import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class ConfigureYouTubeRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/youtube", "youtube", "configure_youtube.html")