package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord

class ConfigureYouTubeRoute(loritta: LorittaDiscord) : GenericConfigurationRoute(loritta, "/configure/youtube", "youtube", "configure_youtube.html")